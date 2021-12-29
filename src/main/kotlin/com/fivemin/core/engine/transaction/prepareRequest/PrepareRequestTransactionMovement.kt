package com.fivemin.core.engine.transaction.prepareRequest

import arrow.core.Either
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.PageNotFoundException
import com.fivemin.core.engine.transaction.PrepareRequestMovement
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
    ): Deferred<Either<Throwable, PrepareTransaction<Document>>> {
        return coroutineScope {
            async {
                logger.debug(source.request.getDebugInfo() + " < Creating prepare transaction")
                preParser.generateInfo(source).toEither { PageNotFoundException() }
            }
        }
    }
}