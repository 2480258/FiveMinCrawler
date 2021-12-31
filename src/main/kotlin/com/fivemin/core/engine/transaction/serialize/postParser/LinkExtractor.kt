package com.fivemin.core.engine.transaction.serialize.postParser

import arrow.core.*
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import java.net.URI

interface LinkExtractor {
    suspend fun extract(resp: ResponseData, sel: Option<LinkSelector>): Either<Throwable, Iterable<LinkExtractedInfo>>
}

class LinkExtractImpl : LinkExtractor {
    private val allNavigator: ParserNavigator = ParserNavigator("*")
    private val linkNavigator: ParserNavigator = ParserNavigator("[href], [src]")

    companion object {
        private val logger = LoggerController.getLogger("LinkExtractImpl")
    }

    override suspend fun extract(
        resp: ResponseData,
        sel: Option<LinkSelector>
    ): Either<Throwable, Iterable<LinkExtractedInfo>> {
        return resp.responseBody.ifSuccAsync({
            it.body.ifHtmlAsync({
                it.parseAsHtmlDocumentAsync {
                    linkExtract(it, resp.responseBody.requestBody.currentUri, sel, ReferrerExtractorStream(resp))
                }
            }, {
                listOf<LinkExtractedInfo>().right()
            })
        }, {
            listOf<LinkExtractedInfo>().right()
        })
    }

    private fun queryWarp(doc: HtmlParsable, selector: Option<LinkSelector>): Iterable<HtmlElement> {
        return selector.fold({
            doc.getElements(allNavigator)
        }, {
            doc.getElements(it.navigator)
        })
    }

    private suspend fun getHrefAndSrc(
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

    private suspend fun linkExtract(
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
            selector.fold({ true }) {
                it.regex.fold({ true }) {
                    it.containsMatchIn(x.absoluteURI.toString())
                }
            }
        }

        return ret
    }

    private suspend fun convert(
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

data class LinkSelector(val navigator: ParserNavigator, val regex: Option<Regex>)

data class LinkExtractedInfo(val absoluteURI: URI, val referrer: String, val additionalTag: TagRepository)
