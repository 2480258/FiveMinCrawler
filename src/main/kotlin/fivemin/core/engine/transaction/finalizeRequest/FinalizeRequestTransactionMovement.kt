package fivemin.core.engine.transaction.finalizeRequest

import arrow.core.Either
import arrow.core.right
import arrow.core.valid
import fivemin.core.LoggerController
import fivemin.core.engine.*
import fivemin.core.engine.transaction.ExecuteRequestMovement
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class FinalizeRequestTransactionMovement<Document : Request>(val requestWaiter : RequestWaiter) : ExecuteRequestMovement<Document> {

    companion object {
        private val logger = LoggerController.getLogger("FinalizeRequestTransactionMovement")
    }

    override suspend fun move(
        source: PrepareTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, FinalizeRequestTransaction<Document>>> {
        val req = DocumentRequestImpl<Document>(source, DocumentRequestInfo(state.isDetachable))
        val ret = requestWaiter.request<Document, ResponseData>(req)

        return coroutineScope {
            async {
                logger.debug(source.request.getDebugInfo() + " < finalizing request transaction")

                val r = ret.await()
                FinalizeRequestTransactionImpl<Document>(r, source.tags, source).right()
            }
        }
    }
}