package core.engine.transaction.serialize.postParser

import core.engine.HtmlElement
import core.engine.HtmlMemoryData
import core.engine.ParserNavigator

interface TextExtractor{
    fun parse(data : HtmlMemoryData, nav : ParserNavigator, mode: TextSelectionMode) : Iterable<String>
}