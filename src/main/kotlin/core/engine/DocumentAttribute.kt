package core.engine

import java.net.URI

fun <T> DocumentAttributeElement.match(ifInte : (DocumentAttributeInternalElement) -> T, ifExt : (DocumentAttributeExternalElement) -> T) : T{
    if(this is DocumentAttributeInternalElement){
        return ifInte(this)
    }
    if(this is DocumentAttributeExternalElement){
        return ifExt(this)
    }

    throw IllegalArgumentException()
}

interface DocumentAttributeFactory{
    fun getInternal(info : DocumentAttributeInfo, data : String) : DocumentAttribute
    fun getInternal(info : DocumentAttributeInfo, data : Iterable<String>) : DocumentAttribute
    fun <Document : Request> getExternal(info: DocumentAttributeInfo, data : FinalizeRequestTransaction<Document>) : DocumentAttribute
    fun <Document : Request> getExternal(info: DocumentAttributeInfo, data: Iterable<FinalizeRequestTransaction<Document>>) : DocumentAttribute
}

data class DocumentAttributeInfo(val name : String){

}

interface DocumentAttribute
{
    val item : DocumentAttributeItem
    val info : DocumentAttributeInfo
}

interface  DocumentAttributeItem : Iterable<DocumentAttributeElement>
{

}

interface  DocumentAttributeElement : DocumentAttribute{

}

interface  DocumentAttributeInternalElement : DocumentAttributeElement{
    val body : String
}

interface  DocumentAttributeExternalElement : DocumentAttributeElement{
    val handle : RequestToken
    val target : URI
    val tagRepo : TagRepository
    val requestOption :RequestOption
    val successInfo : SuccessBody
}

interface  DocumentAttributeSingleItem : DocumentAttributeItem {
    val elem : DocumentAttributeElement
}

interface  DocumentAttributeArrayItem : DocumentAttributeItem {
    val elem : Iterable<DocumentAttributeElement>
}

class DocumentAttributeInternalElementImpl(override val body: String,
                                           override val item: DocumentAttributeItem,
                                           override val info: DocumentAttributeInfo
) : DocumentAttributeInternalElement{

}
