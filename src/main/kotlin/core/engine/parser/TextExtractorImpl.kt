package core.engine.parser

import arrow.core.*
import core.engine.HtmlElement
import core.engine.HtmlMemoryData
import core.engine.ParserNavigator
import core.engine.transaction.serialize.postParser.TextExtractor
import core.engine.transaction.serialize.postParser.TextSelectionMode

class TextSelectionModeNotFoundException
    () : Exception() {

}

class TextExtractorImpl : TextExtractor {
    override fun parse(data: HtmlMemoryData, nav: ParserNavigator, selectionMode: TextSelectionMode): Iterable<String> {
        return data.parseAsHtmlDocument {
            it.getElements(nav).map {
                getMode(it, selectionMode)
            }
        }.fold({ listOf() }, { x -> x }).map {
            it.toOption()
        }.filterOption()
    }

    private fun getMode(elem: HtmlElement, selectionMode: TextSelectionMode): Validated<Throwable, String> {
        return when (selectionMode) {
            TextSelectionMode.INNER_HTML -> {
                elem.innerHtml.valid()
            }
            TextSelectionMode.TEXT_CONTENT -> {
                elem.textContent.valid()
            }
            TextSelectionMode.OUTER_HTML -> {
                elem.outerHtml.valid()
            }
            else -> {
                TextSelectionModeNotFoundException().invalid()
            }
        }
    }
}