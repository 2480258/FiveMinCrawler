package core.initialize

import core.engine.*
import core.engine.transaction.*
import core.engine.transaction.export.ExportParser
import core.engine.transaction.export.ExportTransactionMovement
import core.engine.transaction.finalizeRequest.FinalizeRequestTransactionMovement
import core.engine.transaction.finalizeRequest.RequestWaiter
import core.engine.transaction.prepareRequest.PreParser
import core.engine.transaction.prepareRequest.PrepareRequestTransactionMovement
import core.engine.transaction.serialize.PostParser
import core.engine.transaction.serialize.SerializeTransactionMovementImpl

class MovementFactoryImpl(private val pp : PreParser, private val rw : RequestWaiter, private val ep : ExportParser, private val es : ExportState, private val po : PostParser<Request>) : MovementFactory<Request> {
    override fun findRequest(): ExecuteRequestMovement<Request> {
        return FinalizeRequestTransactionMovement(rw)
    }

    override fun findExport(): ExecuteExportMovement<Request> {
        return ExportTransactionMovement(ep, es)
    }

    override fun findPrepare(): PrepareRequestMovement<Request> {
        return PrepareRequestTransactionMovement(pp)
    }

    override fun findSerialize(): ExecuteSerializeMovement<Request> {
        return SerializeTransactionMovementImpl(po)
    }
}