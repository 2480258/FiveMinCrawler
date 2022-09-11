/*
 *
 *     FiveMinCrawler
 *     Copyright (C) 2022  2480258
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.fivemin.core.engine.transaction.finalizeRequest

import arrow.core.Either
import arrow.core.right
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

class RetrySubPolicy<Document : Request> :
    TransactionSubPolicy<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document> {
    
    private val RETRY_DELAY = 3000L
    
    companion object {
        private val logger = LoggerController.getLogger("RetrySubPolicy")
    }
    
    private suspend fun request(
        source: PrepareTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, FinalizeRequestTransaction<Document>>> {
        logger.debug(source.request, "trying to retry")
        
        return state.retryAsync {
            delay(RETRY_DELAY)
            
            info.createTask<Document>()
                .get1<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>>(source.request.documentType)
                .start(source, info, it)
        }
    }
    
    override suspend fun <Ret> process(
        source: PrepareTransaction<Document>,
        dest: FinalizeRequestTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState,
        next: suspend (Deferred<Either<Throwable, FinalizeRequestTransaction<Document>>>) -> Deferred<Either<Throwable, Ret>>
    ): Deferred<Either<Throwable, Ret>> {
        return next(coroutineScope {
            async {
                dest.result.fold({
                    request(source, info, state).await()
                }, {
                    if (it.responseBody is CriticalErrorBody || it.responseBody is RecoverableErrorBody) {
                        request(source, info, state).await()
                    } else {
                        dest.right()
                    }
                })
            }
        })
    }
}
