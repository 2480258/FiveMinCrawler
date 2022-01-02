package com.fivemin.core.engine.transaction

import arrow.core.Either
import arrow.core.flatten
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import kotlinx.coroutines.*

data class AbstractPolicyOption<
    in SrcTrans : Transaction<Document>,
    DstTrans : StrictTransaction<SrcTrans, Document>,
    out Document : Request>(val subPolicies: Iterable<TransactionSubPolicy<SrcTrans, DstTrans, Document>>)

abstract class AbstractPolicy<
    in SrcTrans : Transaction<Document>,
    DstTrans : StrictTransaction<SrcTrans, Document>,
    Document : Request>(
    private val option: AbstractPolicyOption<SrcTrans, DstTrans, Document>,
    private val movementFactory: MovementFactory<Document>
) : TransactionPolicy<SrcTrans, DstTrans, Document, Document> {
    companion object {
        private val logger = LoggerController.getLogger("AbstractPolicy")
    }

    protected abstract fun getMovement(factory: MovementFactory<Document>): TransactionMovement<SrcTrans, DstTrans, Document>

    override suspend fun progressAsync(
        source: SrcTrans,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, DstTrans>> {
        return coroutineScope {
            async {
                Either.catch {
                    val movement = getMovement(movementFactory)
                    val firstret = movement.move(source, info, state)

                    var ret = option.subPolicies.fold(firstret) { acc, transactionSubPolicy ->
                        coroutineScope {
                            async {
                                var aq = acc.await()

                                aq.map {
                                    transactionSubPolicy.process(source, it, info, state).await()
                                }.flatten()
                            } // https://typelevel.org/cats/datatypes/Either.html
                        }
                    }

                    ret.await()
                }.flatten()
            }
        }
    }
}
