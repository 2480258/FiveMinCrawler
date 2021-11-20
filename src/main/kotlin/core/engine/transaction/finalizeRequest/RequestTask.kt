package core.engine.transaction.finalizeRequest

import core.engine.Request
import core.engine.ResponseData

interface RequestTask {
    fun <Document : Request, GivenResponse : ResponseData> run(request : DocumentRequest<Document>, callback: ResponseCallback<Document, GivenResponse>)
}

data class ResponseCallback<in Document : Request, in GivenResponse : ResponseData>
    (val whenComplete : (Result<GivenResponse>) -> Unit)