package core.engine.transaction.prepareRequest

import arrow.core.Option
import arrow.core.Validated
import arrow.core.invalid
import arrow.core.valid
import core.engine.*
import core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

class DetachableSubPolicy<Document : Request> :
    TransactionSubPolicy<InitialTransaction<Document>, PrepareTransaction<Document>, Document> {
    override suspend fun process(
        source: InitialTransaction<Document>,
        dest: PrepareTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Validated<Throwable, PrepareTransaction<Document>>> {
        return coroutineScope {
            async{
                var ret :  Validated<Throwable, PrepareTransaction<Document>> = dest.valid()


                if (dest.ifDocument({
                        it.containerOption.workingSetMode == WorkingSetMode.Enabled
                    }, { false })) {
                    var task = info.createTask<Document>()
                        .get4<InitialTransaction<Document>, PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, SerializeTransaction<Document>, ExportTransaction<Document>>(
                            dest.request.documentType
                        )

                    var disp = state.ifDetachable {
                        it.detach {
                            task.start(source, info, it).await().swap().toOption()
                        }
                    }

                    ret = coroutineScope {
                        select<Validated<Throwable, PrepareTransaction<Document>>>{
                            disp.fold({dest.valid()}, {
                                it.onAwait.invoke<Option<Throwable>> {
                                    it.fold({dest.valid()}, {it.invalid()})
                                }
                            })
                        }
                    }
                }

                ret
            }
        }

    }
}