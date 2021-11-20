package core.request

import arrow.core.Option
import core.engine.*
import core.request.adapter.RequesterAdapterImpl
import core.request.cookie.*
import kotlinx.coroutines.Deferred
import java.net.URI

class HttpRequestProcedure(info : PerformedRequesterInfo, config : HttpRequesterConfig, val adapter : RequesterAdapter) : CookiedRequester {
    private val cookieRepo : CookieRepository
    private val target : CookieResolveTarget

    init{
        cookieRepo = CookieRepositoryImpl(CustomCookieJar())
        target = config.config.targetFactory.create(info, cookieRepo)
    }

    suspend fun request(request : HttpRequest){
        adapter.requestAsync(request)
    }
}



