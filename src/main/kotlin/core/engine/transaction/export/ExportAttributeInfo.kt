package core.engine.transaction.export;

import arrow.core.Option
import core.engine.DocumentAttributeElement
import core.engine.DocumentAttributeInfo;
import core.engine.TagRepository

data class ExportAttributeInfo (val info: ExportAttributeLocator, val element : DocumentAttributeElement, val tagRepo : TagRepository){

}

data class ExportAttributeLocator (val info : DocumentAttributeInfo, val index : Option<Int>){
    val isList : Boolean = index.isEmpty()
}
