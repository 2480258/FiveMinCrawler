package fivemin.core.engine.transaction.serialize.postParser

import arrow.core.*
import fivemin.core.engine.*
import kotlinx.coroutines.runBlocking
import java.net.URI

interface LinkExtractor {
    fun extract(resp: ResponseData, sel: Option<LinkSelector>): Validated<Throwable, Iterable<LinkExtractedInfo>>
}

class LinkExtractImpl : LinkExtractor {
    private val allNavigator: ParserNavigator = ParserNavigator("*")
    private val linkNavigator: ParserNavigator = ParserNavigator("[href], [src]")

    override fun extract(
        resp: ResponseData,
        sel: Option<LinkSelector>
    ): Validated<Throwable, Iterable<LinkExtractedInfo>> {
        return resp.responseBody.ifSucc({
            it.body.ifHtml({
                it.parseAsHtmlDocument {
                    linkExtract(it, resp.responseBody.requestBody.currentUri, sel, ReferrerExtractorStream(resp))
                }
            }, {
                listOf<LinkExtractedInfo>().valid()
            })
        }, {
            listOf<LinkExtractedInfo>().valid()
        })

    }

    private fun queryWarp(doc: HtmlParsable, selector: Option<LinkSelector>): Iterable<HtmlElement> {
        return selector.fold({
            doc.getElements(allNavigator)
        }, {
            doc.getElements(it.navigator)
        })
    }

    private fun getHrefAndSrc(
        elem: HtmlElement,
        host: URI,
        referrer: ReferrerExtractorStream
    ): Iterable<LinkExtractedInfo> {
        var tag = elem.getElements(linkNavigator)

        var links = tag.map {
            convert(host, it, "href", referrer)
        }.filterOption()

        var srcs = tag.map {
            convert(host, it, "src", referrer)
        }.filterOption()

        return links.plus(srcs).distinct()
    }

    private fun linkExtract(
        parsable: HtmlParsable,
        host: URI,
        selector: Option<LinkSelector>,
        referrer: ReferrerExtractorStream
    ): Iterable<LinkExtractedInfo> {
        var qry = queryWarp(parsable, selector)

        var recur = qry.map {
            getHrefAndSrc(it, host, referrer)
        }.flatten()

        var href = qry.map {
            convert(host, it, "href", referrer)
        }.filterOption()

        var src = qry.map {
            convert(host, it, "src", referrer)
        }.filterOption()

        var ret = recur.plus(href).plus(src).distinctBy {
            it.absoluteURI
        }.filter { x ->
            selector.fold({true}) {
                it.regex.fold({true}) {
                    it.containsMatchIn(x.absoluteURI.toString())
                }
            }
        }

        return ret
    }

    private fun convert(
        host: URI,
        elem: HtmlElement,
        attr: String,
        referrer: ReferrerExtractorStream
    ): Option<LinkExtractedInfo> {
        return makeAbsoluteURI(host, elem, attr).map {
            LinkExtractedInfo(it, referrer.extract(elem), TagRepositoryImpl(none(), none()))
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
                return Some(URI(uri))
            }

            var temp = URI(uri)

            return Some(URI(host.scheme, host.host, temp.path, temp.fragment))
        })
    }

}

data class LinkSelector(val navigator: ParserNavigator, val regex: Option<Regex>)

data class LinkExtractedInfo(val absoluteURI: URI, val referrer: String, val additionalTag: TagRepository)