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
import arrow.core.flatten
import arrow.core.right
import arrow.core.toOption
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.InitialTransactionImpl
import com.fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.net.URI

/**
 * Subpolicy for filters redirect requests and performs them again.
 */
class RedirectSubPolicy<Document : Request> :
    TransactionSubPolicy<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document> {
    
    companion object {
        private val logger = LoggerController.getLogger("RedirectSubPolicy")
    }
    
    private fun createsRedirectURL(
        responseBody: RedirectResponseBody, responseData: ResponseData
    ): URI {
        var loc = responseBody.redirectDest
        
        if (!loc.isAbsolute) {
            loc =
                URI(responseData.responseBody.requestBody.currentUri.scheme + "://" + responseData.responseBody.requestBody.currentUri.authority + loc)
        }
        return loc
    }
    
    override suspend fun <Ret> process(
        source: PrepareTransaction<Document>,
        dest: FinalizeRequestTransaction<Document>,
        
        state: SessionStartedState,
        next: suspend (Either<Throwable, FinalizeRequestTransaction<Document>>) -> Either<Throwable, Ret>
    ): Either<Throwable, Ret> {
        return next(
            dest.result.map { responseData ->
                responseData.responseBody.ifRedirectAsync({ redirectResponseBody ->
                    val redirectLoc = createsRedirectURL(redirectResponseBody, responseData)
                    val doc: Document =
                        source.request.copyWith(redirectLoc.toOption()) as Document //kotlin has no 'self type' so cast it.
                    
                    withContext(Dispatchers.Default) {
                        state.getChildSession {
                            async {
                                logger.info(doc.getDebugInfo() + " < redirect destination")
                                state.taskInfo.createTask<Document>()
                                    .get2<InitialTransaction<Document>, PrepareTransaction<Document>, FinalizeRequestTransaction<Document>>(
                                        doc.documentType
                                    ).start(
                                        InitialTransactionImpl<Document>(
                                            InitialOption(), TagRepositoryImpl(), doc
                                        ), it
                                    ).await()
                            }
                        }
                    }.await()
                }, {
                    withContext(Dispatchers.Default) {
                        dest.right()
                    }
                })
            }.flatten()
        )
    }
}
