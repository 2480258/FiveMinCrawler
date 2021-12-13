package core.engine.transaction.finalizeRequest

import arrow.core.Validated
import arrow.core.flatten
import arrow.core.toOption
import arrow.core.valid
import core.engine.*
import core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.*
import java.net.URI

class RedirectSubPolicy<Document : Request> :
    TransactionSubPolicy<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document> {
    override suspend fun process(
        source: PrepareTransaction<Document>,
        dest: FinalizeRequestTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Validated<Throwable, FinalizeRequestTransaction<Document>>> {
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
                                    info.createTask<Document>()
                                        .get1<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>>(source.request.documentType)
                                        .start(source, info, it).await()
                                }
                            }
                        }.await().toEither()
                    }, {
                        withContext(Dispatchers.Default) {
                            dest.valid()
                        }.toEither()
                    })
                }.toEither().flatten().toValidated()
            }
        }
    }
}