package fivemin.core.engine.transaction.export

import arrow.core.Validated
import fivemin.core.engine.ExportHandle
import fivemin.core.engine.FileIOToken
import fivemin.core.engine.Request
import java.io.OutputStream

interface ExportAdapter {
    fun parse(request : Request, info : Iterable<ExportAttributeInfo>) : Iterable<Validated<Throwable, ExportHandle>>
}

interface ExportHandleFactory{
    fun create(additionalPath : String, ret : OutputStream) : ExportHandle

    fun create(additionalPath: String, ret : String) : ExportHandle

    fun create(additionalPath: String, token : FileIOToken): ExportHandle
}