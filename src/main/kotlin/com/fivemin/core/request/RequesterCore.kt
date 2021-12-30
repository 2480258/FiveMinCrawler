package com.fivemin.core.request

import arrow.core.Either
import com.fivemin.core.engine.ResponseData
import kotlinx.coroutines.Deferred

interface RequesterCore<out Resp : ResponseData> {
    val extraInfo: RequesterExtra

    suspend fun request(request: DequeuedRequest): Deferred<Either<Throwable, Resp>>
}

interface RequesterExtra {
    val dequeueDecision: DequeueDecisionFactory
}
