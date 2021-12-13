package fivemin.core.engine.transaction.serialize.postParser

import arrow.core.Option
import fivemin.core.engine.*
import kotlinx.coroutines.runBlocking

interface InternalContentInfoFactory<in Document : Request> {
    fun get(trans: FinalizeRequestTransaction<Document>): Option<Iterable<InternalContentInfo>>
}

data class InternalContentInfo(val attributeName: String, val data: List<String>) {
}


enum class TextSelectionMode {
    INNER_HTML, OUTER_HTML, TEXT_CONTENT
}

data class InternalContentParser(
    val attributeName: String,
    val nav: ParserNavigator,
    val selectionMode: TextSelectionMode
) {

}

class InternalContentInfoFactoryImpl<Document : Request>(
    private val factories: Iterable<InternalContentParser>,
    private val attributeFactory: DocumentAttributeFactory,
    private val textExtractor: TextExtractor
) : InternalContentInfoFactory<Document> {
    override fun get(trans: FinalizeRequestTransaction<Document>): Option<List<InternalContentInfo>> {
        return runBlocking {
            trans.result.map { y ->
                y.responseBody.ifSuccAsync({ z ->
                    z.body.ifHtml({ a ->
                        factories.map { x ->
                            InternalContentInfo(x.attributeName, textExtractor.parse(a, x.nav, x.selectionMode).toList())
                        }
                    }, { listOf() })
                }, { listOf() })
            }.toOption() //TODO Log
        }
    }
}