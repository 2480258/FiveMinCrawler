package core.engine

import java.io.InputStreamReader
import java.util.stream.Stream

interface  HtmlDocumentFactory{
    fun create(html : String) : HtmlParsable

    fun create(html : InputStreamReader) : HtmlParsable
}

interface HtmlParsable {
    fun getElements(nav: ParserNavigator) : Iterable<HtmlElement>

    fun getElement(nav: ParserNavigator) : HtmlElement?
}

interface HtmlElement{
    val outerHtml : String

    val innerHtml : String

    val textContent : String

    fun getAttribute(name:String) : String
}