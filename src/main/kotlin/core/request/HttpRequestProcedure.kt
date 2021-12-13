package core.request

import arrow.core.Option
import arrow.core.Validated
import core.engine.*
import core.request.adapter.RequesterAdapterImpl
import core.request.cookie.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.net.URI

class HttpRequestProcedure(info: PerformedRequesterInfo, config: HttpRequesterConfig, val adapter: RequesterAdapter) :
    CookiedRequester {
    override val cookieRepository: CookieRepository
    private val target: CookieResolveTarget

    init {
        cookieRepository = CookieRepositoryImpl(CustomCookieJar())
        target = config.config.targetFactory.create(info, cookieRepository)
    }

    suspend fun request(request: Request): Deferred<Validated<Throwable, ResponseBody>> {
        return coroutineScope {
            target.sync {
                adapter.requestAsync(request)
            }
        }
    }
}



