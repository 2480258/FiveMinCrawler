/*
 *
 *     FiveMinCrawler
 *     Copyright (C) 2022  2480258
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.fivemin.core.parser

import arrow.core.Option
import arrow.core.none
import arrow.core.toOption
import com.fivemin.core.engine.HtmlDocumentFactory
import com.fivemin.core.engine.HtmlElement
import com.fivemin.core.engine.HtmlParsable
import com.fivemin.core.engine.ParserNavigator
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.InputStreamReader

class HtmlDocumentFactoryImpl : HtmlDocumentFactory {
    override fun create(html: String): HtmlParsable {
        return HtmlParseableImpl(Jsoup.parse(html, ""))
    }

    override fun create(html: InputStreamReader): HtmlParsable {
        return HtmlParseableImpl(Jsoup.parse(html.readText()))
    }
}

class HtmlParseableImpl(
    val doc: Document
) : HtmlParsable {

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
    val elem: Element
) : HtmlElement {
    override val outerHtml: String
        get() = elem.outerHtml()
    override val innerHtml: String
        get() = elem.html()
    override val textContent: String
        get() = elem.text()

    override fun getAttribute(name: String): Option<String> {
        val attribute = elem.attr(name)

        if (attribute == "") {
            return none()
        }

        return attribute.toOption()
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
