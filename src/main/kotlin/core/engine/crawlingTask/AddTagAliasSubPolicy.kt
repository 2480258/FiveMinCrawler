package core.engine.crawlingTask

import arrow.core.Validated
import core.engine.*
import core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.*

class AddTagAliasSubPolicy<SrcTrans : Transaction<Document>, DstTrans : StrictTransaction<SrcTrans, Document>, Document : Request> :
    TransactionSubPolicy<SrcTrans, DstTrans, Document> {
    override suspend fun process(
        source: SrcTrans,
        dest: DstTrans,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Validated<Throwable, DstTrans>> {
        return coroutineScope {
            async {
                Validated.catch {

                    if (dest is Taggable) {
                        var ret = info.uniqueKeyProvider.tagKey.create(dest.tags)

                        ret.forEach {
                            state.addAlias(it)
                        }
                    }

                    dest
                }

            }
        }


    }

}