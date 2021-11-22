package core.request

import arrow.core.Validated
import core.engine.Request

interface RequestQueue {
    fun enqueue(doc: PreprocessedRequest<Request>, info: EnqueueRequestInfo)
}

data class EnqueueRequestInfo(val callBack : suspend (Validated<Throwable, DequeuedRequest>) -> Unit)
