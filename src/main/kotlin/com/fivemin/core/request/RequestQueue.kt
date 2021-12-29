package com.fivemin.core.request

import arrow.core.Either
import com.fivemin.core.engine.Request

interface RequestQueue {
    fun enqueue(doc: PreprocessedRequest<Request>, info: EnqueueRequestInfo)
}

data class EnqueueRequestInfo(val callBack : suspend (Either<Throwable, DequeuedRequest>) -> Unit)
