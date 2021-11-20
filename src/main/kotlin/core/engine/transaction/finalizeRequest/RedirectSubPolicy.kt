package core.engine.transaction.finalizeRequest

import core.engine.*
import core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.*
import java.net.URI

class RedirectSubPolicy<Document : Request> :
    TransactionSubPolicy<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document> {
    suspend override fun process(
        source: PrepareTransaction<Document>,
        dest: FinalizeRequestTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Result<FinalizeRequestTransaction<Document>>> {
        return coroutineScope {
            withContext(Dispatchers.Default) {
                dest.result.fold<Deferred<Result<FinalizeRequestTransaction<Document>>>, ResponseData>({ x ->
                    x.responseBody.ifRedirect<Deferred<Result<FinalizeRequestTransaction<Document>>>>({ y ->
                        var redirectLoc = y.redirectDest

                        if (!redirectLoc.isAbsolute) {
                            redirectLoc = URI(x.currentUri.authority + redirectLoc)
                        }

                        var doc = source.request.copyWith(redirectLoc)

                        withContext(Dispatchers.Default) {
                            state.getChildSession {
                                async {
                                    info.createTask<Document>()
                                        .get1<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>>(source.request.documentType)
                                        .start(source, info, it).await()
                                }
                            }
                        }

                    }, { async { Result.success(dest) } })
                }, {
                    async { Result.success(dest) }
                })
            }
        }
    }
}