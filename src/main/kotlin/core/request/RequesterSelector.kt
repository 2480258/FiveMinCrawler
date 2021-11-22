package core.request

import arrow.core.Validated
import core.engine.PerformedRequesterInfo
import core.engine.Request
import core.engine.ResponseData
import core.engine.transaction.finalizeRequest.DocumentRequest

interface RequesterSelector {
    fun <Document : Request, Resp : ResponseData> schedule(req : DocumentRequest<Document>) : Validated<Throwable, RequesterSelected<Resp>>
}

data class RequesterSelected<Resp : ResponseData>(val requester : RequesterCore<Resp>, val info : PerformedRequesterInfo)

class AllocationFailedException(str : String) : Exception(str)

class AllocationDelayedException(str : String) : Exception(str)

class AllocationRequesterNotFoundException(str : String) : Exception(str)