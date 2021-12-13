package fivemin.core.request.srtf

import arrow.core.Validated
import fivemin.core.engine.*
import fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class SRTFPrepareSubPolicy(private val sc : SRTFScheduler) : TransactionSubPolicy<InitialTransaction<Request>, PrepareTransaction<Request>, Request> {

    override suspend fun process(
        source: InitialTransaction<Request>,
        dest: PrepareTransaction<Request>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Validated<Throwable, PrepareTransaction<Request>>> {
        return coroutineScope {
            async {
                var det = state.isDetachable == DetachableState.WANT

                Validated.catch {
                    sc.atPrepareStage(dest, det)

                    dest
                }
            }
        }
    }
}