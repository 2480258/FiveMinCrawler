package fivemin.core.engine.transaction.prepareRequest

import fivemin.core.engine.InitialTransaction
import fivemin.core.engine.PrepareTransaction
import fivemin.core.engine.Request
import fivemin.core.engine.transaction.AbstractPolicy
import fivemin.core.engine.transaction.AbstractPolicyOption
import fivemin.core.engine.transaction.MovementFactory
import fivemin.core.engine.transaction.TransactionMovement

class PrepareRequestTransactionPolicy<Document : Request>(
    option: AbstractPolicyOption<InitialTransaction<Document>, PrepareTransaction<Document>, Document>,
    movementFactory: MovementFactory<Document>
) : AbstractPolicy<InitialTransaction<Document>, PrepareTransaction<Document>, Document>(option, movementFactory) {
    override fun getMovement(factory: MovementFactory<Document>): TransactionMovement<InitialTransaction<Document>, PrepareTransaction<Document>, Document> {
        return factory.findPrepare()
    }
}