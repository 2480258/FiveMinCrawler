package core.engine.transaction.prepareRequest

import arrow.core.Validated
import core.engine.*
import core.engine.transaction.PageNotFoundException
import core.engine.transaction.PrepareRequestMovement
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class PrepareRequestTransactionMovement<Document : Request> (private val preParser: PreParser): PrepareRequestMovement<Document> {
    override suspend fun move(
        source: InitialTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Validated<Throwable, PrepareTransaction<Document>>> {
        return coroutineScope {
            async {
                preParser.generateInfo(source).toEither { PageNotFoundException() }.toValidated()
            }
        }
    }
}