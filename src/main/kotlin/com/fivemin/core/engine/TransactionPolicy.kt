package com.fivemin.core.engine

import arrow.core.Either
import kotlinx.coroutines.Deferred

interface TransactionPolicy<in InTrans : Transaction<D1>,
    out OutTrans : Transaction<D2>,
    out D1 : Request,
    out D2 : Request> {
    suspend fun progressAsync(trans: InTrans, info: TaskInfo, state: SessionStartedState): Deferred<Either<Throwable, OutTrans>>
}
