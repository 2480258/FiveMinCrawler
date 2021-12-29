package com.fivemin.core.request.srtf

import arrow.core.Either
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class SRTFExportSubPolicy(private val sc : SRTFScheduler) : TransactionSubPolicy<SerializeTransaction<Request>, ExportTransaction<Request>, Request> {
    override suspend fun process(
        source: SerializeTransaction<Request>,
        dest: ExportTransaction<Request>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, ExportTransaction<Request>>> {
        return coroutineScope {
            async {
                Either.catch {
                    sc.atExportStage(dest.request.token)

                    dest
                }
            }
        }
    }
}