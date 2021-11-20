package core.request

import arrow.core.Validated
import core.engine.ResponseData

interface RequesterCore<out Resp : ResponseData> {
    val extraInfo : RequesterExtra

    fun request(request : DequeuedRequest) : Validated<Throwable, Resp>
}

interface RequesterExtra{
    val dequeueDecision : DequeueDecisionFactory
}