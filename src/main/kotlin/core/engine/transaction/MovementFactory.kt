package core.engine.transaction

import core.engine.Request
import core.engine.StrictTransaction
import core.engine.Transaction

interface MovementFactory<Document : Request> {
    fun <Document : Request> findRequest(): ExecuteRequestMovement<Document>

    fun <Document : Request> findExport(): ExecuteExportMovement<Document>

    fun <Document : Request> findPrepare(): PrepareRequestMovement<Document>

    fun <Document : Request> findSerialize():ExecuteSerializeMovement<Document>
}