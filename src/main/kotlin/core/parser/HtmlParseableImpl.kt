package core.parser

import arrow.core.Option
import arrow.core.invalidNel
import arrow.core.none
import arrow.core.toOption
import core.engine.HtmlDocumentFactory
import core.engine.HtmlElement
import core.engine.HtmlParsable
import core.engine.ParserNavigator
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.InputStreamReader


class HtmlDocumentFactoryImpl() : HtmlDocumentFactory {
    override fun create(html: String): HtmlParsable {
        return HtmlParseableImpl(Jsoup.parse(html))
    }

    override fun create(html: InputStreamReader): HtmlParsable {
        return HtmlParseableImpl(Jsoup.parse(html.readText()))
    }
}

class HtmlParseableImpl(
    val doc : Document
) : HtmlParsable{

    override fun getElements(nav: ParserNavigator): Iterable<HtmlElement> {
        return doc.select(nav.queryStr).map {
            HtmlElementImpl(it)
        }

    }

    override fun getElement(nav: ParserNavigator): Option<HtmlElement> {
        return doc.selectFirst(nav.queryStr).toOption().map {
            HtmlElementImpl(it)
        }
    }

}

class HtmlElementImpl(
    val elem : Element
) : HtmlElement {
    override val outerHtml: String
        get() = elem.outerHtml()
    override val innerHtml: String
        get() = elem.html()
    override val textContent: String
        get() = elem.text()

    override fun getAttribute(name: String): Option<String> {
        var ret = elem.attr(name)

        if(ret == ""){
            return none()
        }

        return ret.toOption()
    }

    override fun getElements(nav: ParserNavigator): Iterable<HtmlElement> {
        return elem.select(nav.queryStr).map {
            HtmlElementImpl(it)
        }
    }

    override fun getElement(nav: ParserNavigator): Option<HtmlElement> {
        return elem.selectFirst(nav.queryStr).toOption().map {
            HtmlElementImpl(it)
        }
    }

}