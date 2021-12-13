package fivemin.core.engine.transaction.serialize.postParser

import fivemin.core.engine.HtmlElement
import fivemin.core.engine.HtmlMemoryData
import fivemin.core.engine.ParserNavigator

interface TextExtractor{
    fun parse(data : HtmlMemoryData, nav : ParserNavigator, mode: TextSelectionMode) : Iterable<String>
}