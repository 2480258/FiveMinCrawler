package com.fivemin.core.engine.transaction.serialize

import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.AbstractPolicy
import com.fivemin.core.engine.transaction.AbstractPolicyOption
import com.fivemin.core.engine.transaction.MovementFactory
import com.fivemin.core.engine.transaction.TransactionMovement

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
