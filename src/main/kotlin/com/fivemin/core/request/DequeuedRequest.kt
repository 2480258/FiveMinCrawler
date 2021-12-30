package com.fivemin.core.request

import com.fivemin.core.engine.Request

data class DequeuedRequest(
    val request: PreprocessedRequest<Request>,
    val info: DequeuedRequestInfo
)

class DequeuedRequestInfo
