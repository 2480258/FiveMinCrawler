package core.engine

import arrow.core.Validated


interface FinalizeRequestTransaction<out Document : Request> :
    ReverableTransaction<PrepareTransaction<Request>, Document>
{
    val result : Validated<Throwable, ResponseData>
}