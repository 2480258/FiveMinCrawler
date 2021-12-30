package com.fivemin.core.engine.transaction

import arrow.core.Either
import com.fivemin.core.engine.*
import kotlinx.coroutines.Deferred

interface TransactionMovement<
    in SrcTrans : Transaction<Request>,
    out DstTrans : StrictTransaction<SrcTrans, Document>,
    out Document : Request> {
    suspend fun move(source: SrcTrans, info: TaskInfo, state: SessionStartedState): Deferred<Either<Throwable, DstTrans>>
}

interface PrepareRequestMovement<Document : Request> :
    TransactionMovement<InitialTransaction<Document>, PrepareTransaction<Document>, Document>

interface ExecuteRequestMovement<Document : Request> :
    TransactionMovement<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document>

interface ExecuteSerializeMovement<Document : Request> :
    TransactionMovement<FinalizeRequestTransaction<Document>, SerializeTransaction<Document>, Document>

interface ExecuteExportMovement<Document : Request> :
    TransactionMovement<SerializeTransaction<Document>, ExportTransaction<Document>, Document>

class PageNotFoundException : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
