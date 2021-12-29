package com.fivemin.core.request

import arrow.core.Option
import com.fivemin.core.engine.PerformedRequesterInfo
import com.fivemin.core.engine.ResponseBody
import com.fivemin.core.engine.ResponseData
import com.fivemin.core.engine.ResponseTime
import okhttp3.Response
import java.net.URI

class HttpResponseMessage(
    override val responseBody: ResponseBody,
    override val requesterInfo: PerformedRequesterInfo
) : ResponseData{
    override fun releaseRequester() {
        return
    }
}

