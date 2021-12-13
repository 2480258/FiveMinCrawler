package fivemin.core.request

import fivemin.core.engine.PerformedRequesterInfo
import fivemin.core.engine.Request
import fivemin.core.engine.transaction.finalizeRequest.DocumentRequest

class PreprocessedRequest<out Document : Request>
    (
    val request: DocumentRequest<Document>,
    val info: PreprocessRequestInfo
) {
}

data class PreprocessRequestInfo(val info: PerformedRequesterInfo, val dequeue: DequeueDecisionFactory) {

}