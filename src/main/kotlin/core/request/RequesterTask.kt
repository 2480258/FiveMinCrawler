package core.request

import arrow.core.Validated
import core.engine.Request
import core.engine.ResponseData
import core.engine.transaction.finalizeRequest.DocumentRequest
import kotlinx.coroutines.Deferred

interface RequesterTask {
    suspend fun <Document : Request, Resp : ResponseData> run(request: DocumentRequest<Document>): Deferred<Validated<Throwable, Resp>>
}