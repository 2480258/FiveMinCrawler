package fivemin.core.request.srtf

import arrow.core.Validated
import fivemin.core.engine.*
import fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class SRTFExportSubPolicy(private val sc : SRTFScheduler) : TransactionSubPolicy<SerializeTransaction<Request>, ExportTransaction<Request>, Request> {
    override suspend fun process(
        source: SerializeTransaction<Request>,
        dest: ExportTransaction<Request>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Validated<Throwable, ExportTransaction<Request>>> {
        return coroutineScope {
            async {
                Validated.catch {
                    sc.atExportStage(dest.request.token)

                    dest
                }
            }
        }
    }
}