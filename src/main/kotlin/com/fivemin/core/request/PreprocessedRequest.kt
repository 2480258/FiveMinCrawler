package com.fivemin.core.request

import com.fivemin.core.engine.PerformedRequesterInfo
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.transaction.finalizeRequest.DocumentRequest

class PreprocessedRequest<out Document : Request>(
    val request: DocumentRequest<Document>,
    val info: PreprocessRequestInfo
)

data class PreprocessRequestInfo(val info: PerformedRequesterInfo, val dequeue: DequeueDecisionFactory)
