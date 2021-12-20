package fivemin.core.engine.transaction.prepareRequest

import arrow.core.Option
import arrow.core.Validated
import arrow.core.invalid
import arrow.core.valid
import fivemin.core.LoggerController
import fivemin.core.engine.*
import fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import mu.KotlinLogging

class TaskDetachedException : Exception() {}

class DetachableSubPolicy<Document : Request> :
    TransactionSubPolicy<InitialTransaction<Document>, PrepareTransaction<Document>, Document> {

    companion object {
        private val logger = LoggerController.getLogger("DetachableSubPolicy")
    }

    override suspend fun process(
        source: InitialTransaction<Document>,
        dest: PrepareTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Validated<Throwable, PrepareTransaction<Document>>> {
        return coroutineScope {
            async {
                val ret = if (dest.ifDocument({
                        it.containerOption.workingSetMode == WorkingSetMode.Enabled
                    }, { false })) {
                    var task = info.createTask<Document>()
                        .get4<InitialTransaction<Document>, PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, SerializeTransaction<Document>, ExportTransaction<Document>>(
                            dest.request.documentType
                        )

                    var disp = state.ifDetachable {
                        it.detach {
                            logger.info(source.request.getDebugInfo() + " < trying to detach")

                            task.start(source, info, it).await().swap().toOption()
                        }
                    }

                    if(disp.isNotEmpty()) {
                        TaskDetachedException().invalid()
                    } else {
                        dest.valid()
                    }
                } else {
                    dest.valid()
                }

                ret
            }
        }

    }
}