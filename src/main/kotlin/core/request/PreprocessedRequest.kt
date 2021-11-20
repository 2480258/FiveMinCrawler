package core.request

import core.engine.PerformedRequesterInfo
import core.engine.Request
import core.engine.transaction.finalizeRequest.DocumentRequest

interface PreprocessedRequest<out Document : Request> {
    val info : PreprocessRequestInfo
    val request : DocumentRequest<Document>
}

data class PreprocessRequestInfo(val info : PerformedRequesterInfo, val dequeue : DequeueDecisionFactory){

}