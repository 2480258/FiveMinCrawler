package com.fivemin.core.engine.transaction.finalizeRequest

import com.fivemin.core.engine.FinalizeRequestTransaction
import com.fivemin.core.engine.PrepareTransaction
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.transaction.*

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
