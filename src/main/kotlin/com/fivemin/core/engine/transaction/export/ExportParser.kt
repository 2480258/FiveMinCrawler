package com.fivemin.core.engine.transaction.export

import arrow.core.singleOrNone
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.ExportHandle
import com.fivemin.core.engine.ExportTransaction
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.SerializeTransaction

interface ExportParser {
    fun <Document : Request> parse(trans: SerializeTransaction<Document>): Iterable<ExportHandle>
}

class ExportParserImpl(private val pages: Iterable<ExportPage>) : ExportParser {
    
    companion object {
        private val logger = LoggerController.getLogger("AddTagAliasSubPolicy")
    }
    
    override fun <Document : Request> parse(trans: SerializeTransaction<Document>): Iterable<ExportHandle> {
        var pg = pages.filter {
            it.isAcceptable(trans)
        }
        
        if(!pg.any()) {
            logger.warn(trans.request, "no matched export pages. ignoring....")
        }
        
        return pg.flatMap {
            it.export(trans)
        }
    }
}