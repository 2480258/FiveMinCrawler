package fivemin.core.engine.transaction.serialize.postParser

import arrow.core.*
import fivemin.core.engine.*
import kotlinx.coroutines.runBlocking

class ReferrerExtractorStream(private val resp: ResponseData) {
    val HEADER_REFERRER_TAGS = "Referrer-Policy"
    val referrerNavigator: ParserNavigator

    init {
        referrerNavigator = ParserNavigator("meta[name = referrer]")
    }

    fun extract(link: HtmlElement): String {
        var r = link.getAttribute("referrerpolicy").fold({
            link.getAttribute("rel").map {
                if (it == "noreferrer") {
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

    private fun parseGlobal():
            Option<String> {
        return runBlocking {
            resp.responseBody.ifSuccAsync({ x ->
                var ret = x.body.ifHtml({
                    it.parseAsHtmlDocument {
                        it.getElement(referrerNavigator)
                    }
                }, {
                    none<HtmlElement>().valid()
                })

                Some(ret)
            }, {
                none()
            }).map {
                //TODO: Log
                it.toOption()
            }.flatten().flatten().map {
                it.getAttribute("content")
            }.flatten()
        }
    }
}