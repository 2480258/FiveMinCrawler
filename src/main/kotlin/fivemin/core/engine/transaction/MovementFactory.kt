package fivemin.core.engine.transaction

import fivemin.core.engine.Request
import fivemin.core.engine.StrictTransaction
import fivemin.core.engine.Transaction

interface MovementFactory<Document : Request> {
    fun <Document : Request> findRequest(): ExecuteRequestMovement<Document>

    fun <Document : Request> findExport(): ExecuteExportMovement<Document>

    fun <Document : Request> findPrepare(): PrepareRequestMovement<Document>

    fun <Document : Request> findSerialize():ExecuteSerializeMovement<Document>
}