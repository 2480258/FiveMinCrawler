package com.fivemin.core.request

import arrow.core.Either
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.ResponseBody
import kotlinx.coroutines.Deferred

interface RequesterAdapter {
    suspend fun requestAsync(uri : Request) : Deferred<Either<Throwable, ResponseBody>>
}

