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
import com.fivemin.core.engine.transaction.ExecuteRequestMovement
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class FinalizeRequestTransactionMovement<Document : Request>(val requestWaiter: RequestWaiter) : ExecuteRequestMovement<Document> {

    companion object {
        private val logger = LoggerController.getLogger("FinalizeRequestTransactionMovement")
    }

    override suspend fun move(
        source: PrepareTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, FinalizeRequestTransaction<Document>>> {
        val req = DocumentRequestImpl<Document>(source, DocumentRequestInfo(state.isDetachable))
        val ret = requestWaiter.request<Document, ResponseData>(req)

        return coroutineScope {
            async {
                logger.debug(source.request, "finalizing request transaction")

                val r = ret.await()
                FinalizeRequestTransactionImpl<Document>(r, source.tags, source).right()
            }
        }
    }
}
