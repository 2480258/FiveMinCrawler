package core.request

import arrow.core.Validated
import core.engine.ResponseData
import kotlinx.coroutines.Deferred

interface RequesterCore<out Resp : ResponseData> {
    val extraInfo : RequesterExtra

    suspend fun request(request : DequeuedRequest) : Deferred<Validated<Throwable, Resp>>
}

interface RequesterExtra{
    val dequeueDecision : DequeueDecisionFactory
}