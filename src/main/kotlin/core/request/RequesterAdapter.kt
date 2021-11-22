package core.request

import arrow.core.Validated
import core.engine.Request
import core.engine.ResponseBody
import kotlinx.coroutines.Deferred

interface RequesterAdapter {
    suspend fun requestAsync(uri : Request) : Deferred<Validated<Throwable, ResponseBody>>
}

