package core.engine.transaction.finalizeRequest

import arrow.core.Validated
import core.engine.Request
import core.engine.ResponseData
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore

class RequestWaiter(private val requestTaskFactory: RequestTaskFactory) {

    suspend fun <Document : Request, GivenResponse : ResponseData> request(request: DocumentRequest<Document>) : Deferred<Validated<Throwable, GivenResponse>> {
        var task = requestTaskFactory.create()
        return task.run(request)
    }
}