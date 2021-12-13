package fivemin.core.request

import arrow.core.Validated
import fivemin.core.engine.PerformedRequesterInfo
import fivemin.core.engine.Request
import fivemin.core.engine.ResponseData
import fivemin.core.engine.transaction.finalizeRequest.DocumentRequest

interface RequesterSelector {
    fun <Document : Request, Resp : ResponseData> schedule(req : DocumentRequest<Document>) : Validated<Throwable, RequesterSelected<Resp>>
}

data class RequesterSelected<Resp : ResponseData>(val requester : RequesterCore<Resp>, val info : PerformedRequesterInfo)

class AllocationFailedException(str : String) : Exception(str)

class AllocationDelayedException(str : String) : Exception(str)

class AllocationRequesterNotFoundException(str : String) : Exception(str)