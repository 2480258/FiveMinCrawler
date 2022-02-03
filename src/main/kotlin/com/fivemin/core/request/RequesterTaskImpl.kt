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

package com.fivemin.core.request

import arrow.core.Either
import arrow.core.flatten
import arrow.core.left
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.ResponseData
import com.fivemin.core.engine.transaction.finalizeRequest.DocumentRequest
import kotlinx.coroutines.Deferred

data class RequestTaskOption(val selector: RequesterSelector, val queue: RequestQueue)

class RequesterTaskImpl(private val option: RequestTaskOption) : RequesterTask {
    override suspend fun <Document : Request, Resp : ResponseData> run(request: DocumentRequest<Document>): Deferred<Either<Throwable, Resp>> {
        var handle = TaskWaitHandle<Either<Throwable, Resp>>()

        return handle.run {
            option.selector.schedule<Document, Resp>(request).map { x ->
                var preprocess =
                    PreprocessedRequest(request, PreprocessRequestInfo(x.info, x.requester.extraInfo.dequeueDecision))
                option.queue.enqueue(
                    preprocess,
                    EnqueueRequestInfo { y ->
                        var ret = y.map {
                            try {
                                x.requester.request(it).await()
                            } catch (e: Exception) {
                                e.left()
                            }
                        }.flatten()

                        handle.registerResult(ret)
                    }
                )
            }
        }
    }
}
