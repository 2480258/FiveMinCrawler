package fivemin.core.engine

import arrow.core.Validated
import kotlinx.coroutines.Deferred
import kotlin.reflect.KClass
import kotlin.reflect.KType

interface TransactionPolicy<in InTrans : Transaction<D1>,
                            out OutTrans : Transaction<D2>,
                            out D1 : Request,
                            out D2 : Request>
{
    suspend fun progressAsync(trans : InTrans, info : TaskInfo, state : SessionStartedState) : Deferred<Validated<Throwable, OutTrans>>
}