package core.engine.transaction.serialize

import arrow.core.Validated
import core.engine.*
import core.engine.transaction.AbstractPolicy
import core.engine.transaction.AbstractPolicyOption
import core.engine.transaction.MovementFactory
import core.engine.transaction.TransactionMovement
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