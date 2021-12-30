package com.fivemin.core.engine.transaction.prepareRequest

import arrow.core.*
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class TaskDetachedException : Exception()

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
    ): Deferred<Either<Throwable, PrepareTransaction<Document>>> {
        return coroutineScope {
            async {
                val ret = if (dest.ifDocument({
                    it.containerOption.workingSetMode == WorkingSetMode.Enabled
                }, { false })
                ) {
                    var task = info.createTask<Document>()
                        .get4<InitialTransaction<Document>, PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, SerializeTransaction<Document>, ExportTransaction<Document>>(
                            dest.request.documentType
                        )

                    var disp = state.ifDetachable {
                        it.detach {
                            logger.debug(source.request.getDebugInfo() + " < trying to detach")

                            task.start(source, info, it).await().swap().orNull().toOption()
                        }
                    }

                    if (disp.isNotEmpty()) {
                        TaskDetachedException().left()
                    } else {
                        dest.right()
                    }
                } else {
                    dest.right()
                }

                ret
            }
        }
    }
}
