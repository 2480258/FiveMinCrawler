package core.engine.transaction.finalizeRequest

import core.engine.FinalizeRequestTransaction
import core.engine.PrepareTransaction
import core.engine.Request
import core.engine.transaction.*

class FinalizeRequestTransactionPolicy<Document : Request>(
    option: AbstractPolicyOption<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document>,
    movementFactory: MovementFactory<Document>
) : AbstractPolicy<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document>(
    option,
    movementFactory
) {
    override fun getMovement(factory: MovementFactory<Document>): TransactionMovement<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document> {
        return factory.findPrepare<Document>()
    }
}