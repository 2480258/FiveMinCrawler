package com.fivemin.core.engine.transaction.serialize.postParser

import arrow.core.*
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import java.net.URI

interface LinkExtractor {
    suspend fun extract(resp: ResponseData, sel: Option<LinkSelector>): Either<Throwable, Iterable<LinkExtractedInfo>>
}

interface LinkParser {
    fun parse(parsable: HtmlParsable, host: URI, selector: Option<LinkSelector>): Iterable<ParsedLink>
}

data class ParsedLink(val absoluteUri: URI, val referrerInfo: ReferrerInfo)

data class ReferrerInfo(val referrerPolicy: Option<String>, val rel: Option<String>)

class LinkExtractImpl(private val linkParser: LinkParser) : LinkExtractor {

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
                    val parsedLink = linkParser.parse(it, resp.responseBody.requestBody.currentUri, sel).map {
                        convert(it, ReferrerExtractorStream(resp))
                    }

                    parsedLink
                }
            }, {
                listOf<LinkExtractedInfo>().right()
            })
        }, {
            listOf<LinkExtractedInfo>().right()
        })
    }

    private suspend fun convert(
        parsedLink: ParsedLink,
        referrer: ReferrerExtractorStream
    ): LinkExtractedInfo {
        return LinkExtractedInfo(parsedLink.absoluteUri, referrer.extract(parsedLink), TagRepositoryImpl(none(), none()))
    }
}

data class LinkSelector(val navigator: ParserNavigator, val regex: Option<Regex>)

data class LinkExtractedInfo(val absoluteURI: URI, val referrer: String, val additionalTag: TagRepository)
