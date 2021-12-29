package fivemin.core.engine.transaction.serialize.postParser

import arrow.core.Option
import arrow.core.toOption
import fivemin.core.LoggerController
import fivemin.core.engine.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

interface InternalContentInfoFactory<in Document : Request> {
    suspend fun get(trans: FinalizeRequestTransaction<Document>): Option<Iterable<InternalContentInfo>>
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
    
    companion object {
        private val logger = LoggerController.getLogger("InternalContentInfoFactoryImpl")
    }
    
    
    override suspend fun get(trans: FinalizeRequestTransaction<Document>): Option<List<InternalContentInfo>> {
        return coroutineScope {
            var ret = trans.result.map { y ->
                y.responseBody.ifSuccAsync({ z ->
                    z.body.ifFile({ //remove temp file because anyway it should be read
                        it.file.remove()
                    }, { })
                    
                    z.body.ifHtml({ a ->
                        factories.map { x ->
                            InternalContentInfo(x.attributeName, textExtractor.parse(a, x.nav, x.selectionMode).toList())
                        }
                    }, { listOf() })
                }, { listOf() })
            }
            
            ret.swap().map {
                logger.warn(trans.request.getDebugInfo() + " < can't extract internal attribute from due to: " + it)
            }
            
            ret.orNull().toOption()
        }
    }
}