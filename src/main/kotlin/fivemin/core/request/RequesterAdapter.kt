package fivemin.core.request

import arrow.core.Either
import fivemin.core.engine.Request
import fivemin.core.engine.ResponseBody
import kotlinx.coroutines.Deferred

interface RequesterAdapter {
    suspend fun requestAsync(uri : Request) : Deferred<Either<Throwable, ResponseBody>>
}

