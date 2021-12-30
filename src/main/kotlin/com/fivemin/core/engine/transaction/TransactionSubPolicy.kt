package com.fivemin.core.engine.transaction

import arrow.core.Either
import com.fivemin.core.engine.*
import kotlinx.coroutines.Deferred

interface TransactionSubPolicy <in SrcTrans : Transaction<Document>,
    DstTrans : StrictTransaction<SrcTrans, Document>,
    out Document : Request> {
    suspend fun process(source: SrcTrans, dest: DstTrans, info: TaskInfo, state: SessionStartedState): Deferred<Either<Throwable, DstTrans>>
}
