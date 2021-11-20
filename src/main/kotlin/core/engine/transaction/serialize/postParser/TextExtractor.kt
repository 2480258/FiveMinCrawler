package core.engine.transaction.serialize.postParser

import core.engine.HtmlMemoryData
import core.engine.ParserNavigator

interface TextExtractor{
    fun parse(data : HtmlMemoryData, nav : ParserNavigator, selectionMode: TextSelectionMode) : Iterable<String>
}