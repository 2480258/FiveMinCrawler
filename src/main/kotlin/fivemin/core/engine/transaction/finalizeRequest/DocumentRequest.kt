package fivemin.core.engine.transaction.finalizeRequest

import fivemin.core.engine.DetachableState
import fivemin.core.engine.PrepareTransaction
import fivemin.core.engine.Request

interface DocumentRequest<out Document : Request> {
    val request : PrepareTransaction<Document>

    val info : DocumentRequestInfo
}

data class DocumentRequestInfo(val detachState: DetachableState)


data class DocumentRequestImpl<Document : Request>(
    override val request: PrepareTransaction<Document>,
    override val info: DocumentRequestInfo
) : DocumentRequest<Document>{

}