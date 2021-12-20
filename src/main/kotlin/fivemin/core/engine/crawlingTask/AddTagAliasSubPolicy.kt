package fivemin.core.engine.crawlingTask

import arrow.core.Validated
import fivemin.core.LoggerController
import fivemin.core.engine.*
import fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.*
import mu.KotlinLogging

class AddTagAliasSubPolicy<SrcTrans : Transaction<Document>, DstTrans : StrictTransaction<SrcTrans, Document>, Document : Request> :
    TransactionSubPolicy<SrcTrans, DstTrans, Document> {

    companion object {
        private val logger = LoggerController.getLogger("AddTagAliasSubPolicy")
    }

    override suspend fun process(
        source: SrcTrans,
        dest: DstTrans,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Validated<Throwable, DstTrans>> {
        return coroutineScope {
            async {
                Validated.catch {
                    var ret = info.uniqueKeyProvider.tagKey.create(dest.tags)

                    ret.forEach {
                        state.addAlias(it)

                        logger.info(source.request.getDebugInfo() + " < [Tag]" + it.toString())
                    }

                    dest
                }

            }
        }


    }

}