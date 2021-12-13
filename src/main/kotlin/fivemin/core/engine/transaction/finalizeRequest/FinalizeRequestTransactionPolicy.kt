package fivemin.core.engine.transaction.finalizeRequest

import fivemin.core.engine.FinalizeRequestTransaction
import fivemin.core.engine.PrepareTransaction
import fivemin.core.engine.Request
import fivemin.core.engine.transaction.*

class FinalizeRequestTransactionPolicy<Document : Request>(
    option: AbstractPolicyOption<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document>,
    movementFactory: MovementFactory<Document>
) : AbstractPolicy<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document>(
    option,
    movementFactory
) {
    override fun getMovement(factory: MovementFactory<Document>): TransactionMovement<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document> {
        return factory.findRequest()
    }
}