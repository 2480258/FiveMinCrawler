package core.engine.transaction.prepareRequest

import core.engine.InitialTransaction
import core.engine.PrepareTransaction
import core.engine.Request
import core.engine.transaction.AbstractPolicy
import core.engine.transaction.AbstractPolicyOption
import core.engine.transaction.MovementFactory
import core.engine.transaction.TransactionMovement

class PrepareRequestTransactionPolicy<Document : Request>(
    option: AbstractPolicyOption<InitialTransaction<Document>, PrepareTransaction<Document>, Document>,
    movementFactory: MovementFactory<Document>
) : AbstractPolicy<InitialTransaction<Document>, PrepareTransaction<Document>, Document>(option, movementFactory) {
    override fun getMovement(factory: MovementFactory<Document>): TransactionMovement<InitialTransaction<Document>, PrepareTransaction<Document>, Document> {
        return factory.findPrepare()
    }
}