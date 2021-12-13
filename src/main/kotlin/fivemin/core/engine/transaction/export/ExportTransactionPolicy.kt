package fivemin.core.engine.transaction.export

import fivemin.core.engine.ExportTransaction
import fivemin.core.engine.Request
import fivemin.core.engine.SerializeTransaction
import fivemin.core.engine.transaction.AbstractPolicy
import fivemin.core.engine.transaction.AbstractPolicyOption
import fivemin.core.engine.transaction.MovementFactory
import fivemin.core.engine.transaction.TransactionMovement

class ExportTransactionPolicy<Document : Request>(private val option : AbstractPolicyOption<SerializeTransaction<Document>, ExportTransaction<Document>, Document>, factory: MovementFactory<Document>) :
    AbstractPolicy<SerializeTransaction<Document>, ExportTransaction<Document>, Document>(option, factory) {
    override fun getMovement(factory: MovementFactory<Document>): TransactionMovement<SerializeTransaction<Document>, ExportTransaction<Document>, Document> {
        return factory.findExport()
    }
}