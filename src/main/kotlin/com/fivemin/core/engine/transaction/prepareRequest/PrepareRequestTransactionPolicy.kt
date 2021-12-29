package com.fivemin.core.engine.transaction.prepareRequest

import com.fivemin.core.engine.InitialTransaction
import com.fivemin.core.engine.PrepareTransaction
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.transaction.AbstractPolicy
import com.fivemin.core.engine.transaction.AbstractPolicyOption
import com.fivemin.core.engine.transaction.MovementFactory
import com.fivemin.core.engine.transaction.TransactionMovement

class PrepareRequestTransactionPolicy<Document : Request>(
    option: AbstractPolicyOption<InitialTransaction<Document>, PrepareTransaction<Document>, Document>,
    movementFactory: MovementFactory<Document>
) : AbstractPolicy<InitialTransaction<Document>, PrepareTransaction<Document>, Document>(option, movementFactory) {
    override fun getMovement(factory: MovementFactory<Document>): TransactionMovement<InitialTransaction<Document>, PrepareTransaction<Document>, Document> {
        return factory.findPrepare()
    }
}