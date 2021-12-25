package fivemin.core.engine.crawlingTask

import arrow.core.Either
import arrow.core.right
import arrow.core.valid
import fivemin.core.LoggerController
import fivemin.core.engine.*
import fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging

class MarkDetachablePolicy<Document : Request> :
    TransactionSubPolicy<InitialTransaction<Document>, PrepareTransaction<Document>, Document> {

    companion object {
        private val logger = LoggerController.getLogger("SessionDetachable")
    }

    override suspend fun process(
        source: InitialTransaction<Document>,
        dest: PrepareTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, PrepareTransaction<Document>>> {
        if (dest.ifDocument({
                it.containerOption.workingSetMode == WorkingSetMode.Enabled
            }, { false })) {
            logger.debug(source.request.getDebugInfo() + " < Marked as detachable")
            state.setDetachable()
        } else {
            logger.debug(source.request.getDebugInfo() + " < Marked as non-detachable")
            state.setNonDetachable()
        }

        return coroutineScope {
            async {
                dest.right()
            }
        }
    }
}