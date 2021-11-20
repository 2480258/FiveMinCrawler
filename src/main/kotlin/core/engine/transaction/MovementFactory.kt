package core.engine.transaction

import core.engine.Request
import core.engine.StrictTransaction
import core.engine.Transaction

interface MovementFactory<Document : Request> {
    fun <SrcTrans : Transaction<Document>, DstTrans : StrictTransaction<SrcTrans, Document>> find() : TransactionMovement<SrcTrans, DstTrans, Document>
}