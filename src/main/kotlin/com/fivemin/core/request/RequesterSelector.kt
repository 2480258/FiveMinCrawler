package com.fivemin.core.request

import arrow.core.Either
import com.fivemin.core.engine.PerformedRequesterInfo
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.ResponseData
import com.fivemin.core.engine.transaction.finalizeRequest.DocumentRequest

interface RequesterSelector {
    fun <Document : Request, Resp : ResponseData> schedule(req : DocumentRequest<Document>) : Either<Throwable, RequesterSelected<Resp>>
}

data class RequesterSelected<Resp : ResponseData>(val requester : RequesterCore<Resp>, val info : PerformedRequesterInfo)

class AllocationFailedException(str : String) : Exception(str)

class AllocationDelayedException(str : String) : Exception(str)

class AllocationRequesterNotFoundException(str : String) : Exception(str)