package fivemin.core.engine.transaction.export

import arrow.core.Invalid
import arrow.core.Valid
import arrow.core.Validated
import fivemin.core.engine.ExportHandle
import fivemin.core.engine.Request
import fivemin.core.engine.ifFile
import fivemin.core.engine.match

class BinaryExportAdapter (private val fileName : TagExpression, private val factory : ExportHandleFactory) : ExportAdapter{
    override fun parse(request: Request, info : Iterable<ExportAttributeInfo>) : Iterable<Validated<Throwable, ExportHandle>>{
        val ret = info.map<ExportAttributeInfo, Validated<Throwable, ExportHandle>> { x ->
            x.element.match<Validated<Throwable, ExportHandle>>({Invalid(IllegalArgumentException())}, { y->
                y.successInfo.body.ifFile<Validated<Throwable, ExportHandle>>({ z ->
                                           Valid(factory.create(fileName.build(x.tagRepo), z.file))
                }, { z ->

                    z.openWriteStreamUnsafe().map{
                        factory.create(fileName.build(x.tagRepo), it)
                    }
                })
            })
        }

        return ret
    }
}
