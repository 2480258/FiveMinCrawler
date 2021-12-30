package com.fivemin.core.request

import com.fivemin.core.engine.PerformedRequesterInfo
import com.fivemin.core.engine.ResponseBody
import com.fivemin.core.engine.ResponseData

class HttpResponseMessage(
    override val responseBody: ResponseBody,
    override val requesterInfo: PerformedRequesterInfo
) : ResponseData {
    override fun releaseRequester() {
        return
    }
}
