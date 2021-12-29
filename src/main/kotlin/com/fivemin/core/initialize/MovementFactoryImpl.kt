package com.fivemin.core.initialize

import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.*
import com.fivemin.core.engine.transaction.export.ExportParser
import com.fivemin.core.engine.transaction.export.ExportTransactionMovement
import com.fivemin.core.engine.transaction.finalizeRequest.FinalizeRequestTransactionMovement
import com.fivemin.core.engine.transaction.finalizeRequest.RequestWaiter
import com.fivemin.core.engine.transaction.prepareRequest.PreParser
import com.fivemin.core.engine.transaction.prepareRequest.PrepareRequestTransactionMovement
import com.fivemin.core.engine.transaction.serialize.PostParser
import com.fivemin.core.engine.transaction.serialize.SerializeTransactionMovementImpl

class MovementFactoryImpl(private val pp : PreParser, private val rw : RequestWaiter, private val ep : ExportParser, private val es : ExportState, private val po : PostParser<Request>) : MovementFactory<Request> {
    override fun <Document : Request> findRequest(): ExecuteRequestMovement<Document> {
        return FinalizeRequestTransactionMovement(rw)
    }

    override fun <Document : Request> findExport(): ExecuteExportMovement<Document> {
        return ExportTransactionMovement(ep, es)
    }

    override fun <Document : Request> findPrepare(): PrepareRequestMovement<Document> {
        return PrepareRequestTransactionMovement(pp)
    }

    override fun <Document : Request> findSerialize(): ExecuteSerializeMovement<Document> {
        return SerializeTransactionMovementImpl(po)
    }

}