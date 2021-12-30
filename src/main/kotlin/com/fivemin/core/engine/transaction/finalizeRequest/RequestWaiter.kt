package com.fivemin.core.engine.transaction.finalizeRequest

import arrow.core.Either
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.RequesterTaskFactory
import com.fivemin.core.engine.ResponseData
import kotlinx.coroutines.Deferred

class RequestWaiter(private val requestTaskFactory: RequesterTaskFactory) {

    suspend fun <Document : Request, GivenResponse : ResponseData> request(request: DocumentRequest<Document>): Deferred<Either<Throwable, GivenResponse>> {
        var task = requestTaskFactory.create()
        return task.run(request)
    }
}
