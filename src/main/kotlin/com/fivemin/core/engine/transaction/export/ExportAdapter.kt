package com.fivemin.core.engine.transaction.export

import arrow.core.Either
import com.fivemin.core.engine.ExportHandle
import com.fivemin.core.engine.FileIOToken
import com.fivemin.core.engine.Request
import java.io.InputStream

interface ExportAdapter {
    fun parse(request : Request, info : Iterable<ExportAttributeInfo>) : Iterable<Either<Throwable, ExportHandle>>
}

interface ExportHandleFactory{
    fun create(additionalPath : String, ret : InputStream) : ExportHandle

    fun create(additionalPath: String, ret : String) : ExportHandle

    fun create(additionalPath: String, token : FileIOToken): ExportHandle
}