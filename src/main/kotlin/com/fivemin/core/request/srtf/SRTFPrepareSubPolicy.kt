package com.fivemin.core.request.srtf

import arrow.core.Either
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class SRTFPrepareSubPolicy(private val sc: SRTFScheduler) : TransactionSubPolicy<InitialTransaction<Request>, PrepareTransaction<Request>, Request> {

    companion object {
        private val logger = LoggerController.getLogger("SRTFPrepareSubPolicy")
    }

    override suspend fun process(
        source: InitialTransaction<Request>,
        dest: PrepareTransaction<Request>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, PrepareTransaction<Request>>> {
        return coroutineScope {
            async {
                var det = state.isDetachable == DetachableState.WANT

                var ret = Either.catch {
                    sc.atPrepareStage(dest, det)

                    dest
                }

                ret.swap().map {
                    logger.warn(it)
                }

                ret
            }
        }
    }
}
