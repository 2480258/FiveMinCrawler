package fivemin.core.engine.transaction

import arrow.core.Validated
import fivemin.core.engine.*
import kotlinx.coroutines.Deferred

interface TransactionSubPolicy <in SrcTrans : Transaction<Document>,
        DstTrans : StrictTransaction<SrcTrans, Document>,
        out Document : Request>{
    suspend fun process(source : SrcTrans, dest : DstTrans, info : TaskInfo, state : SessionStartedState) : Deferred<Validated<Throwable, DstTrans>>
}