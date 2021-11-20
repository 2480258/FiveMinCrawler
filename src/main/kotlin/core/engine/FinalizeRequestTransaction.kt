package core.engine


interface FinalizeRequestTransaction<out Document : Request> :
    ReverableTransaction<PrepareTransaction<Request>, Document>
{
    val result : Result<ResponseData>
}