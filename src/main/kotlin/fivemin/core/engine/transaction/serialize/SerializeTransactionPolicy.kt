package fivemin.core.engine.transaction.serialize

import arrow.core.Either
import fivemin.core.engine.*
import fivemin.core.engine.transaction.AbstractPolicy
import fivemin.core.engine.transaction.AbstractPolicyOption
import fivemin.core.engine.transaction.MovementFactory
import fivemin.core.engine.transaction.TransactionMovement
import kotlinx.coroutines.Deferred
import kotlin.reflect.KClass
import kotlin.reflect.KType

class SerializeTransactionPolicy<Document : Request>(
    private val option: AbstractPolicyOption<FinalizeRequestTransaction<Document>, SerializeTransaction<Document>, Document>,
    private val movementFactory: MovementFactory<Document>
) :
    AbstractPolicy<FinalizeRequestTransaction<Document>, SerializeTransaction<Document>, Document>(
        option,
        movementFactory
    ) {
    override fun getMovement(factory: MovementFactory<Document>): TransactionMovement<FinalizeRequestTransaction<Document>, SerializeTransaction<Document>, Document> {
        return factory.findSerialize()
    }
}