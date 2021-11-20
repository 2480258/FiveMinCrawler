package core.engine.transaction.serialize.postParser

import core.engine.DocumentAttributeFactory
import core.engine.FinalizeRequestTransaction
import core.engine.Request
import core.engine.ParserNavigator

interface InternalContentInfoFactory<in Document : Request>{
    fun get(trans : FinalizeRequestTransaction<Document>) : Iterable<InternalContentInfo>
}

data class InternalContentInfo(val attributeName : String, val data : Iterable<String>){
}


enum class TextSelectionMode{
    INNER_HTML, OUTER_HTML, TEXT_CONTENT
}

data class InternalContentParser(val attributeName: String, val nav : ParserNavigator, val selectionMode: TextSelectionMode){

}

class InternalContentinfoFactoryImpl<Document : Request> (private val factories : Iterable<InternalContentParser>,
private val attributeFactory: DocumentAttributeFactory,
private val textExtractor : TextExtractor
): InternalContentInfoFactory<Document>{
    override fun get(trans: FinalizeRequestTransaction<Document>): Iterable<InternalContentInfo> {
        TODO("Not yet implemented")
    }

}