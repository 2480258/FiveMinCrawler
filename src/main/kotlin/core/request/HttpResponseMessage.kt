package core.request

import arrow.core.Option
import core.engine.PerformedRequesterInfo
import core.engine.ResponseBody
import core.engine.ResponseData
import core.engine.ResponseTime
import okhttp3.Response
import java.net.URI

class HttpResponseMessage(
    private val response : Option<Response>,
    override val requesterInfo: PerformedRequesterInfo
) : ResponseData{
    override val responseBody: ResponseBody

    init{
        response.
    }


    override fun releaseRequester() {
        return
    }
}

