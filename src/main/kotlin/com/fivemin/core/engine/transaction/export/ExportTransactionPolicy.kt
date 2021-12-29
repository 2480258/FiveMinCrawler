package com.fivemin.core.engine.transaction.export

import com.fivemin.core.engine.ExportTransaction
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.SerializeTransaction
import com.fivemin.core.engine.transaction.AbstractPolicy
import com.fivemin.core.engine.transaction.AbstractPolicyOption
import com.fivemin.core.engine.transaction.MovementFactory
import com.fivemin.core.engine.transaction.TransactionMovement

class ExportTransactionPolicy<Document : Request>(private val option : AbstractPolicyOption<SerializeTransaction<Document>, ExportTransaction<Document>, Document>, factory: MovementFactory<Document>) :
    AbstractPolicy<SerializeTransaction<Document>, ExportTransaction<Document>, Document>(option, factory) {
    override fun getMovement(factory: MovementFactory<Document>): TransactionMovement<SerializeTransaction<Document>, ExportTransaction<Document>, Document> {
        return factory.findExport()
    }
}