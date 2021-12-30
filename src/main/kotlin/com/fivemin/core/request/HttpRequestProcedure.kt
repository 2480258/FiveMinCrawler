package com.fivemin.core.request

import arrow.core.Either
import com.fivemin.core.engine.PerformedRequesterInfo
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.ResponseBody
import com.fivemin.core.request.cookie.CookieRepository
import com.fivemin.core.request.cookie.CookieRepositoryImpl
import com.fivemin.core.request.cookie.CookieResolveTarget
import com.fivemin.core.request.cookie.CookiedRequester
import com.fivemin.core.request.cookie.CustomCookieJar
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.coroutineScope

class HttpRequestProcedure(info: PerformedRequesterInfo, config: HttpRequesterConfig, val adapter: RequesterAdapter) :
    CookiedRequester {
    override val cookieRepository: CookieRepository
    private val target: CookieResolveTarget

    init {
        cookieRepository = CookieRepositoryImpl(CustomCookieJar())
        target = config.config.targetFactory.create(info, cookieRepository)
    }

    suspend fun request(request: Request): Deferred<Either<Throwable, ResponseBody>> {
        return coroutineScope {
            target.sync {
                adapter.requestAsync(request)
            }
        }
    }
}
