package core.engine.transaction.serialize

import core.engine.FinalizeRequestTransaction
import core.engine.Request
import core.engine.SerializeTransaction
import core.engine.transaction.AbstractPolicy
import core.engine.transaction.AbstractPolicyOption
import core.engine.transaction.MovementFactory
import core.engine.transaction.TransactionMovement

class SerializeTransactionPolicyImpl<Document : Request>(private val option : AbstractPolicyOption<FinalizeRequestTransaction<Document>, SerializeTransaction<Document>, Document>,
                                                         private val movementFactory: MovementFactory<Document>) :
    AbstractPolicy<FinalizeRequestTransaction<Document>, SerializeTransaction<Document>, Document>(option, movementFactory) {
    override fun getMovement(factory: MovementFactory<Document>): TransactionMovement<FinalizeRequestTransaction<Document>, SerializeTransaction<Document>, Document> {
        return factory.find()
    }
}