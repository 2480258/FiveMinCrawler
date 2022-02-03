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

package com.fivemin.core.engine.transaction.serialize.postParser.linkExtract

import arrow.core.Option
import arrow.core.Some
import arrow.core.filterOption
import arrow.core.none
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.HtmlElement
import com.fivemin.core.engine.HtmlParsable
import com.fivemin.core.engine.ParserNavigator
import com.fivemin.core.engine.transaction.serialize.postParser.*
import java.net.URI

class LinkParserImpl : LinkParser {
    private val allNavigator: ParserNavigator = ParserNavigator("*")
    private val linkNavigator: ParserNavigator = ParserNavigator("[href], [src]")

    companion object {
        private val logger = LoggerController.getLogger("LinkParserImpl")
    }

    private val HREF_ATTR = "href"
    private val SRC_ATTR = "src"

    private val REFERRERPOLICY_ATTR = "referrerpolicy"
    private val REL_ATTR = "rel"

    override fun parse(
        parsable: HtmlParsable,
        host: URI,
        selector: Option<LinkSelector>
    ): Iterable<ParsedLink> {
        val suspects = selector.fold({
            parsable.getElements(allNavigator)
        }, {
            parsable.getElements(it.navigator)
        })

        val extractedLinks = suspects.map {
            getHrefAndSrc(it, host)
        }.flatten()

        var uniqueUris = extractedLinks.distinctBy {
            it.absoluteUri
        }.filter { x ->
            selector.fold({ true }) {
                it.regex.fold({ true }) {
                    it.containsMatchIn(x.absoluteUri.toString())
                }
            }
        }

        return uniqueUris
    }

    private fun getHrefAndSrc(
        elem: HtmlElement,
        host: URI
    ): Iterable<ParsedLink> {
        var tag = elem.getElements(linkNavigator)

        var links = tag.map {
            convert(host, it, HREF_ATTR)
        }.filterOption()

        var srcs = tag.map {
            convert(host, it, SRC_ATTR)
        }.filterOption()

        return links.plus(srcs).distinct()
    }

    private fun convert(
        host: URI,
        elem: HtmlElement,
        attr: String
    ): Option<ParsedLink> {
        return makeAbsoluteURI(host, elem, attr).map {
            ParsedLink(it, ReferrerInfo(elem.getAttribute(REFERRERPOLICY_ATTR), elem.getAttribute(REL_ATTR)))
        }
    }

    private fun makeAbsoluteURI(host: URI, elem: HtmlElement, attributeName: String): Option<URI> {
        val tag = elem.getAttribute(attributeName)

        return tag.fold({ none<URI>() }, {
            var uri = it

            if (uri.contains("#")) {
                uri = uri.split("#")[0]
            }

            if (uri.contains("://") && uri.contains("http")) {
                try {
                    return Some(URI(uri))
                } catch (e: Exception) {
                    logger.warn("can't get URL from given string: $uri $e")

                    return none<URI>()
                }
            }

            var temp: URI? = null
            var path: String? = null

            return try {
                temp = URI(uri)

                path = temp.path

                if (path.first() != '/') {
                    path = "/$path"
                }

                Some(URI(host.scheme, null, host.host, host.port, path, temp.query, null))
            } catch (e: Exception) {
                logger.warn("can't get URL from given string: " + host.scheme + ", " + host.host + ", " + (path ?: "null") + ", " + temp?.query)
                logger.warn("URL is: " + (temp ?: "null"))
                none<URI>()
            }
        })
    }
}
