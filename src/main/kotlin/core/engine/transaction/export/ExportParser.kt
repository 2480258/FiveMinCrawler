package core.engine.transaction.export

import core.engine.ExportHandle
import core.engine.ExportTransaction
import core.engine.Request
import core.engine.SerializeTransaction

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