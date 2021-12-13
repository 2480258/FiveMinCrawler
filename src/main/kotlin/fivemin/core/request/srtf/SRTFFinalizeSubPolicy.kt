package fivemin.core.request.srtf

import arrow.core.Validated
import fivemin.core.engine.*
import fivemin.core.engine.transaction.TransactionSubPolicy
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
    ): Deferred<Validated<Throwable, FinalizeRequestTransaction<Request>>> {
        return coroutineScope {
            async {
                Validated.catch {
                    sc.atFinalizeStage(dest, state.isDetachable == DetachableState.WANT)

                    dest
                }

            }
        }
    }
}