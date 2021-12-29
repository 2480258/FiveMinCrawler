package com.fivemin.core.engine.transaction.export

import com.fivemin.core.engine.ExportHandle
import com.fivemin.core.engine.ExportTransaction
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.SerializeTransaction

interface ExportParser {
    fun <Document : Request> parse(trans : SerializeTransaction<Document>) : Iterable<ExportHandle>
}

class ExportParserImpl(private val pages : Iterable<ExportPage>) : ExportParser{
    override fun <Document : Request> parse(trans: SerializeTransaction<Document>): Iterable<ExportHandle> {
        var pg = pages.single {
            it.isAcceptable(trans)
        }
    
        return pg.export(trans)
    }
}