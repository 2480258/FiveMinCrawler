package fivemin.core.request

import arrow.core.Either
import fivemin.core.engine.Request
import fivemin.core.engine.ResponseData
import fivemin.core.engine.transaction.finalizeRequest.DocumentRequest
import kotlinx.coroutines.Deferred

interface RequesterTask {
    suspend fun <Document : Request, Resp : ResponseData> run(request: DocumentRequest<Document>): Deferred<Either<Throwable, Resp>>
}