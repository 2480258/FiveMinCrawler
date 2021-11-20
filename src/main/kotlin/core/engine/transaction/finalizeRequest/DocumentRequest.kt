package core.engine.transaction.finalizeRequest

import core.engine.DetachableState
import core.engine.PrepareTransaction
import core.engine.Request

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