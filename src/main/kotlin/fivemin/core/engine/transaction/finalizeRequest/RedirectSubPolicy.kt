package fivemin.core.engine.transaction.finalizeRequest

import arrow.core.*
import fivemin.core.LoggerController
import fivemin.core.engine.*
import fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.*
import java.net.URI

class RedirectSubPolicy<Document : Request> :
    TransactionSubPolicy<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document> {

    companion object {
        private val logger = LoggerController.getLogger("RedirectSubPolicy")
    }

    override suspend fun process(
        source: PrepareTransaction<Document>,
        dest: FinalizeRequestTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, FinalizeRequestTransaction<Document>>> {
        return coroutineScope {
            async {
                dest.result.map { x ->
                    x.responseBody.ifRedirectAsync({ y ->
                        var loc = y.redirectDest

                        if (!loc.isAbsolute) {
                            loc = URI(x.responseBody.requestBody.currentUri.authority + loc)
                        }

                        var doc = source.request.copyWith(loc.toOption())

                        withContext(Dispatchers.Default) {
                            state.getChildSession {
                                async {
                                    logger.info(source.request.getDebugInfo() + " < redirect destination")
                                    info.createTask<Document>()
                                        .get1<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>>(source.request.documentType)
                                        .start(source, info, it).await()
                                }
                            }
                        }.await()
                    }, {
                        withContext(Dispatchers.Default) {
                            dest.right()
                        }
                    })
                }.flatten()
            }
        }
    }
}