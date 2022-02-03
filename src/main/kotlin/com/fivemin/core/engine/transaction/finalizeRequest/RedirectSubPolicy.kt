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
import com.fivemin.core.engine.transaction.InitialTransactionImpl
import com.fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.*
import java.net.URI

class RedirectSubPolicy<Document : Request> :
    TransactionSubPolicy<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document> {

    companion object {
        private val logger = LoggerController.getLogger("RedirectSubPolicy")
    }

    override suspend fun process(
        source: PrepareTransaction<Document>,
        dest: FinalizeRequestTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, FinalizeRequestTransaction<Document>>> {
        return coroutineScope {
            async {
                dest.result.map { x ->
                    x.responseBody.ifRedirectAsync({ y ->
                        var loc = y.redirectDest

                        if (!loc.isAbsolute) {
                            loc = URI(x.responseBody.requestBody.currentUri.scheme + "://" + x.responseBody.requestBody.currentUri.authority + loc)
                        }

                        var doc: Document = source.request.copyWith(loc.toOption()) as Document

                        withContext(Dispatchers.Default) {
                            state.getChildSession {
                                async {
                                    logger.info(doc.getDebugInfo() + " < redirect destination")
                                    info.createTask<Document>().get2<
                                        InitialTransaction<Document>,
                                        PrepareTransaction<Document>,
                                        FinalizeRequestTransaction<Document>>(
                                        doc.documentType
                                    )
                                        .start(InitialTransactionImpl<Document>(InitialOption(), TagRepositoryImpl(), doc), info, it).await()
                                }
                            }
                        }.await()
                    }, {
                        withContext(Dispatchers.Default) {
                            dest.right()
                        }
                    })
                }.flatten()
            }
        }
    }
}
