package com.fivemin.core.engine

import arrow.core.Either

interface FinalizeRequestTransaction<out Document : Request> :
    ReverableTransaction<PrepareTransaction<Request>, Document>
{
    override val previous: PrepareTransaction<Document>
    val result : Either<Throwable, ResponseData>
}