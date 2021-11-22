package core.engine.transaction.finalizeRequest

import arrow.core.Validated
import arrow.core.valid
import core.engine.*
import core.engine.transaction.ExecuteRequestMovement
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class FinalizeRequestTransactionMovement<Document : Request>(val requestWaiter : RequestWaiter) : ExecuteRequestMovement<Document> {
    override suspend fun move(
        source: PrepareTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Validated<Throwable, FinalizeRequestTransaction<Document>>> {
        val req = DocumentRequestImpl<Document>(source, DocumentRequestInfo(state.isDetachable))
        val ret = requestWaiter.request<Document, ResponseData>(req)

        return coroutineScope {
            async {
                val r = ret.await()
                FinalizeRequestTransactionImpl<Document>(r, source.tags, source).valid()
            }
        }
    }
}