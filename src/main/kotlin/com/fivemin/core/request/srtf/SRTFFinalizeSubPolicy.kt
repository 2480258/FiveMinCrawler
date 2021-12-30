package com.fivemin.core.request.srtf

import arrow.core.Either
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class SRTFFinalizeSubPolicy(private val sc: SRTFScheduler) :
    TransactionSubPolicy<PrepareTransaction<Request>, FinalizeRequestTransaction<Request>, Request> {
    override suspend fun process(
        source: PrepareTransaction<Request>,
        dest: FinalizeRequestTransaction<Request>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, FinalizeRequestTransaction<Request>>> {
        return coroutineScope {
            async {
                Either.catch {
                    sc.atFinalizeStage(dest, state.isDetachable == DetachableState.WANT)

                    dest
                }
            }
        }
    }
}
