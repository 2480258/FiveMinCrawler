package core.engine.transaction

import arrow.core.Validated
import arrow.core.flatMap
import core.engine.*
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select

data class AbstractPolicyOption<
        in SrcTrans : Transaction<Document>,
        DstTrans : StrictTransaction<SrcTrans, Document>,
        out Document : Request>(val subPolicies: Iterable<TransactionSubPolicy<SrcTrans, DstTrans, Document>>) {

}

abstract class AbstractPolicy<
        in SrcTrans : Transaction<Document>,
        DstTrans : StrictTransaction<SrcTrans, Document>,
        Document : Request>
    (
    private val option: AbstractPolicyOption<SrcTrans, DstTrans, Document>,
    private val movementFactory: MovementFactory<Document>
) {

    protected abstract fun getMovement(factory: MovementFactory<Document>): TransactionMovement<SrcTrans, DstTrans, Document>

    suspend fun progress(
        source: SrcTrans,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Validated<Throwable, DstTrans>> {
        val movement = getMovement(movementFactory)
        val firstret = movement.move(source, info, state)


        return option.subPolicies.fold(firstret) { acc, transactionSubPolicy ->
            coroutineScope {
                async {
                    acc.await().toEither().flatMap {
                        transactionSubPolicy.process(source, it, info, state).await().toEither()
                    }.toValidated()
                } //https://typelevel.org/cats/datatypes/validated.html
            }
        }
    }
}