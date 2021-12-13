package core.engine.transaction.finalizeRequest

import arrow.core.Validated
import arrow.core.valid
import core.engine.*
import core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.*

class ResponseDisposeSubPolicy<Document : Request> :
    TransactionSubPolicy<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document> {
    override suspend fun process(
        source: PrepareTransaction<Document>,
        dest: FinalizeRequestTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Validated<Throwable, FinalizeRequestTransaction<Document>>> {

        return coroutineScope {
            async {
                Validated.catch {
                    dest.result.map {
                        it.releaseRequester()
                    }

                    dest
                }
            }
        }
    }
}