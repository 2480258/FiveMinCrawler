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
import arrow.core.computations.either
import arrow.core.flatten
import arrow.core.identity
import arrow.core.left
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.ResponseData
import com.fivemin.core.engine.transaction.finalizeRequest.DocumentRequest
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class RequestTaskOption(val selector: RequesterSelector, val queue: RequestQueue)


class RequesterTaskImpl(private val option: RequestTaskOption) : RequesterTask {
    
    /**
     * Non-blocking, Asynchronously downloads a request at the GlobalScope.
     *
     * Many exceptions will be caught (i.e Connection errors) but some critical errors will be thrown (i.e Cancellation Exception).
     *
     * To cancel request, just call cancel() on the returned Deferred<T>
     * */
    override suspend fun <Document : Request, Resp : ResponseData> run(request: DocumentRequest<Document>): Deferred<Either<Throwable, Resp>> {
        var handle = TaskWaitHandle<Either<Throwable, Resp>>()
        
        val mappedRequester = option.selector.schedule<Document, Resp>(request)
        val preprocess = mappedRequester.map {
            PreprocessedRequest(request, PreprocessRequestInfo(it.info, it.requester.extraInfo.dequeueDecision))
        }
        
        val enqueue = either<Throwable, EnqueueRequestInfo> {
            val req = mappedRequester.bind()
            
            EnqueueRequestInfo { info ->
                try {
                    val ret = info.map {
                        req.requester.request(it).await()
                    }.flatten()
    
                    handle.registerResult(ret)
                } catch (e: Exception) {
                    handle.registerResult(e.left())
                } finally {
                    handle.forceFinishIfNot()
                }
            }
        }
        
        val result = either<Throwable, Deferred<Either<Throwable, Resp>>> {
            val enq = enqueue.bind()
            val pre = preprocess.bind()
            
            handle.runAsync({
                option.queue.enqueue(pre, enq)
            }, {
                option.queue.cancelWSSet(pre.request)
            })
        }
        
        return result.fold({
            coroutineScope { async { it.left() } }
        }, ::identity)
    }
}
