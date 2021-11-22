package core.request

import arrow.core.Validated
import arrow.core.invalid
import arrow.core.valid
import core.engine.Request
import core.engine.ResponseData
import core.engine.transaction.finalizeRequest.DocumentRequest
import kotlinx.coroutines.Deferred

data class RequestTaskOption(val selector: RequesterSelector, val queue: RequestQueue)

class DefaultRequesterTask(private val option: RequestTaskOption) : RequesterTask {
    override suspend fun <Document : Request, Resp : ResponseData> run(request: DocumentRequest<Document>):  Deferred<Validated<Throwable, Resp>> {
        var handle = TaskWaitHandle<Validated<Throwable, Resp>>()
        return handle.run {
            option.selector.schedule<Document, Resp>(request).map { x ->
                var preprocess =
                    PreprocessedRequest(request, PreprocessRequestInfo(x.info, x.requester.extraInfo.dequeueDecision))
                option.queue.enqueue(preprocess, EnqueueRequestInfo { y ->
                    y.bimap({ z ->
                        handle.registerResult(z.invalid())
                    }) { z ->
                        var ret = x.requester.request(z).await()
                        handle.registerResult(ret)
                    }
                })
            }
        }
    }
}