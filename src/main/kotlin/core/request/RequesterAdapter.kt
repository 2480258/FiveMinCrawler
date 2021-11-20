package core.request

import arrow.core.Validated
import core.engine.HttpRequest
import core.engine.ResponseBody
import kotlinx.coroutines.Deferred

interface RequesterAdapter {
    suspend fun requestAsync(uri : HttpRequest) : Deferred<Validated<Throwable, ResponseBody>>
}

