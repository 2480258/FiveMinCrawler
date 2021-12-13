package fivemin.core.engine

import arrow.core.Validated

interface FinalizeRequestTransaction<out Document : Request> :
    ReverableTransaction<PrepareTransaction<Request>, Document>
{
    override val previous: PrepareTransaction<Document>
    val result : Validated<Throwable, ResponseData>
}