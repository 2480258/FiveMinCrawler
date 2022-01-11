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
