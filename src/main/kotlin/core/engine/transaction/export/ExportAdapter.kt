package core.engine.transaction.export

import arrow.core.Validated
import core.engine.ExportHandle
import core.engine.FileIOToken
import core.engine.Request
import java.io.OutputStream

interface ExportAdapter {
    fun parse(request : Request, info : Iterable<ExportAttributeInfo>) : Iterable<Validated<Throwable, ExportHandle>>
}

interface ExportHandleFactory{
    fun create(additionalPath : String, ret : OutputStream) : ExportHandle

    fun create(additionalPath: String, ret : String) : ExportHandle

    fun create(additionalPath: String, token : FileIOToken): ExportHandle
}