package fivemin.core.engine.transaction.export

import fivemin.core.engine.ExportHandle
import fivemin.core.engine.ExportTransaction
import fivemin.core.engine.Request
import fivemin.core.engine.SerializeTransaction

interface ExportParser {
    fun <Document : Request> parse(trans : SerializeTransaction<Document>) : Iterable<ExportHandle>
}

class ExportParserImpl(private val pages : Iterable<ExportPage>) : ExportParser{
    override fun <Document : Request> parse(trans: SerializeTransaction<Document>): Iterable<ExportHandle> {
        return pages.flatMap {
            it.export(trans)
        }
    }
}