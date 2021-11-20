package core.engine.transaction.finalizeRequest

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
    ): Deferred<Result<FinalizeRequestTransaction<Document>>> {

        return coroutineScope {
            async {
                dest.result.onSuccess {
                    it.releaseRequester()
                }
                Result.success(dest)
            }
        }
    }
}