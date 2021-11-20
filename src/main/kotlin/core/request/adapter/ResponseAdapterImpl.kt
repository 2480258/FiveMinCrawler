package core.request.adapter

import arrow.core.Option
import core.engine.PerformedRequesterInfo
import core.engine.ResponseBody
import core.request.AutomaticRedirectResponseBodyImpl
import core.request.NetworkHeader
import core.request.SuccessBodyImpl
import okhttp3.RequestBody
import okhttp3.Response
import java.net.URI

class ResponseAdapterImpl (private val performedRequesterInfo: PerformedRequesterInfo){



    fun create(original : URI, resp : Response, req : Option<RequestBody>) : ResponseBody{

    }

    private fun createInfo(original: URI, resp: Response, req: Option<RequestBody>) : ResponseBody{
        req.map {
            if(resp.request.url.toUri() != original && resp.body != null){
                return AutomaticRedirectResponseBodyImpl(it, resp.code, NetworkHeader(resp.headers.asIterable().toList()), createInfo(resp.request.url.toUri(), resp, req))
            }

            if(resp.body != null && resp.isSuccessful){
                return SuccessBodyImpl(it, resp.code, NetworkHeader(resp.headers.asIterable().toList()), )
            }
        }

    }

}

