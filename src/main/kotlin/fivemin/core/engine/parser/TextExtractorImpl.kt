package fivemin.core.engine.parser

import arrow.core.*
import fivemin.core.engine.HtmlElement
import fivemin.core.engine.HtmlMemoryData
import fivemin.core.engine.ParserNavigator
import fivemin.core.engine.transaction.serialize.postParser.TextExtractor
import fivemin.core.engine.transaction.serialize.postParser.TextSelectionMode

class TextSelectionModeNotFoundException
    () : Exception() {

}

class TextExtractorImpl : TextExtractor {
    override fun parse(data: HtmlMemoryData, nav: ParserNavigator, mode : TextSelectionMode): Iterable<String> {
        var ret = data.parseAsHtmlDocument {
            it.getElements(nav).map {
                it
            }
        }.fold({ listOf() }, { x -> x }).map {
            it.toOption()
        }.filterOption()

        return ret.map {
            selectMode(it, mode)
        }
    }

    private fun selectMode(elem : HtmlElement, mode: TextSelectionMode) : String{
        return when(mode){
            TextSelectionMode.TEXT_CONTENT -> elem.textContent
            TextSelectionMode.OUTER_HTML -> elem.outerHtml
            TextSelectionMode.INNER_HTML -> elem.innerHtml
        }
    }
}