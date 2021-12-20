package fivemin.core.engine.transaction.prepareRequest

import arrow.core.Validated
import fivemin.core.LoggerController
import fivemin.core.engine.*
import fivemin.core.engine.transaction.PageNotFoundException
import fivemin.core.engine.transaction.PrepareRequestMovement
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class PrepareRequestTransactionMovement<Document : Request> (private val preParser: PreParser): PrepareRequestMovement<Document> {

    companion object {
        private val logger = LoggerController.getLogger("PrepareRequestTransactionMovement")
    }

    override suspend fun move(
        source: InitialTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Validated<Throwable, PrepareTransaction<Document>>> {
        return coroutineScope {
            async {
                logger.debug(source.request.getDebugInfo() + " < Creating prepare transaction")
                preParser.generateInfo(source).toEither { PageNotFoundException() }.toValidated()
            }
        }
    }
}