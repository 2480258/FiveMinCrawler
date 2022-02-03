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

package com.fivemin.core.engine.transaction.serialize.postParser

import arrow.core.*
import com.fivemin.core.engine.*

class ReferrerExtractorStream(private val resp: ResponseData) {
    val HEADER_REFERRER_TAGS = "Referrer-Policy"
    val referrerNavigator: ParserNavigator

    init {
        referrerNavigator = ParserNavigator("meta[name = referrer]")
    }

    suspend fun extract(link: ParsedLink): String {
        var r = link.referrerInfo.referrerPolicy.fold({
            link.referrerInfo.rel.map {
                if (it.contains("noreferrer")) {
                    Some("no-referrer")
                } else {
                    none()
                }
            }.flatten()
        }, {
            Some(it)
        })

        return r.getOrElse { parseGlobal().getOrElse { parseHeader().getOrElse { "strict-origin-when-cross-origin" } } }
    }

    private fun parseHeader(): Option<String> {
        var body = resp.responseBody

        if (body is HttpResponseReceivedBody) {
            return body.responseHeader.header.singleOrNull {
                it.first.lowercase() == HEADER_REFERRER_TAGS.lowercase()
            }?.second.toOption()
        }

        return none()
    }

    private suspend fun parseGlobal():
        Option<String> {
        return resp.responseBody.ifSuccAsync({ x ->
            var ret = x.body.ifHtml({
                it.parseAsHtmlDocument {
                    it.getElement(referrerNavigator)
                }
            }, {
                none<HtmlElement>().right()
            })

            Some(ret)
        }, {
            none()
        }).map {
            it.orNull().toOption()
        }.flatten().flatten().map {
            it.getAttribute("content")
        }.flatten()
    }
}
