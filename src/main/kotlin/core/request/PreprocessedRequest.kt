package core.request

import core.engine.PerformedRequesterInfo
import core.engine.Request
import core.engine.transaction.finalizeRequest.DocumentRequest

class PreprocessedRequest<out Document : Request>
    (
    val request: DocumentRequest<Document>,
    val info: PreprocessRequestInfo
) {
}

data class PreprocessRequestInfo(val info: PerformedRequesterInfo, val dequeue: DequeueDecisionFactory) {

}