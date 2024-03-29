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

import arrow.core.*
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.ExecuteRequestMovement
import com.fivemin.core.logger.Log
import com.fivemin.core.logger.LogLevel

class FinalizeRequestTransactionMovement<Document : Request>(val requestWaiter: RequestWaiter) :
    ExecuteRequestMovement<Document> {
    
    override suspend fun <Ret> move(
        source: PrepareTransaction<Document>,
        
        state: SessionStartedState,
        next: suspend (Either<Throwable, FinalizeRequestTransaction<Document>>) -> Either<Throwable, Ret>
    ): Either<Throwable, Ret> {
        var dest: Either<Throwable, FinalizeRequestTransaction<Document>>? = null
        
        try {
            val req = DocumentRequestImpl<Document>(source, DocumentRequestInfo(state.isDetachable))
            val ret = requestWaiter.request<Document, ResponseData>(req)
            
            dest = FinalizeRequestTransactionImpl<Document>(ret.await(), source.tags, source).right()
            val result = next(dest)
            
            return result
            
        } finally {
            dest?.map {
                releaseRequester(it)
            }
        }
    }
    
    @Log(
        beforeLogLevel = LogLevel.DEBUG,
        afterReturningLogLevel = LogLevel.DEBUG,
        afterThrowingLogLevel = LogLevel.ERROR,
        beforeMessage = "releasing requester",
        afterThrowingMessage = "failed to release requester"
    )
    private fun releaseRequester(dest: FinalizeRequestTransaction<Document>) {
        dest.result.map {
            it.releaseRequester()
        }
    }
}
