package fivemin.core.request

import arrow.core.Option
import fivemin.core.engine.PerformedRequesterInfo
import fivemin.core.engine.ResponseBody
import fivemin.core.engine.ResponseData
import fivemin.core.engine.ResponseTime
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

