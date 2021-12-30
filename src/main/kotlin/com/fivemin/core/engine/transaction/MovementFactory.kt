package com.fivemin.core.engine.transaction

import com.fivemin.core.engine.Request

interface MovementFactory<Document : Request> {
    fun <Document : Request> findRequest(): ExecuteRequestMovement<Document>

    fun <Document : Request> findExport(): ExecuteExportMovement<Document>

    fun <Document : Request> findPrepare(): PrepareRequestMovement<Document>

    fun <Document : Request> findSerialize(): ExecuteSerializeMovement<Document>
}
