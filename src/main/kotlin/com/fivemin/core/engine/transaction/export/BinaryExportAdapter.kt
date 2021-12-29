package com.fivemin.core.engine.transaction.export

import arrow.core.Invalid
import arrow.core.Valid
import arrow.core.Either
import com.fivemin.core.engine.ExportHandle
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.ifFile
import com.fivemin.core.engine.match

class BinaryExportAdapter(private val fileName: TagExpression, private val factory: ExportHandleFactory) :
    ExportAdapter {
    override fun parse(
        request: Request,
        info: Iterable<ExportAttributeInfo>
    ): Iterable<Either<Throwable, ExportHandle>> {
        val ret = info.map<ExportAttributeInfo, Either<Throwable, ExportHandle>> { x ->
            x.element.match<Either<Throwable, ExportHandle>>({ Either.Left(IllegalArgumentException()) }, { y ->
                y.successInfo.body.ifFile<Either<Throwable, ExportHandle>>({ z ->
                    Either.Right(factory.create(fileName.build(x.tagRepo), z.file))
                }, { z ->
                    
                    z.openStreamAsByteAndDispose {
                        factory.create(fileName.build(x.tagRepo), it)
                    }
                })
            })
        }
        
        return ret
    }
}
