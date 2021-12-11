package core.engine.transaction.export

import core.engine.ExportTransaction
import core.engine.Request
import core.engine.SerializeTransaction
import core.engine.transaction.AbstractPolicy
import core.engine.transaction.AbstractPolicyOption
import core.engine.transaction.MovementFactory
import core.engine.transaction.TransactionMovement

class ExportTransactionPolicy<Document : Request>(private val option : AbstractPolicyOption<SerializeTransaction<Document>, ExportTransaction<Document>, Document>, factory: MovementFactory<Document>) :
    AbstractPolicy<SerializeTransaction<Document>, ExportTransaction<Document>, Document>(option, factory) {
    override fun getMovement(factory: MovementFactory<Document>): TransactionMovement<SerializeTransaction<Document>, ExportTransaction<Document>, Document> {
        return factory.findExport()
    }
}