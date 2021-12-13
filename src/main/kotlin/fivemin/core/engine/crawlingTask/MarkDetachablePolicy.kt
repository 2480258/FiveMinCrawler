package fivemin.core.engine.crawlingTask

import arrow.core.Validated
import arrow.core.valid
import fivemin.core.engine.*
import fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class MarkDetachablePolicy<Document : Request> :
    TransactionSubPolicy<InitialTransaction<Document>, PrepareTransaction<Document>, Document> {
    override suspend fun process(
        source: InitialTransaction<Document>,
        dest: PrepareTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Validated<Throwable, PrepareTransaction<Document>>> {
        if (dest.ifDocument({
                it.containerOption.workingSetMode == WorkingSetMode.Enabled
            }, { false })) {
            state.setDetachable()
        } else {
            state.setNonDetachable()
        }

        return coroutineScope {
            async {
                dest.valid()
            }
        }
    }
}