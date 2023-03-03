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

    override suspend fun extract(
        resp: ResponseData,
        sel: Option<LinkSelector>
    ): Either<Throwable, Iterable<LinkExtractedInfo>> {
        return resp.responseBody.ifSuccAsync({ successBody ->
            successBody.body.ifHtmlAsync({ htmlMemoryData ->
                htmlMemoryData.parseAsHtmlDocumentAsync { htmlParsable ->
                    val parsedLink = linkParser.parse(htmlParsable, resp.responseBody.requestBody.currentUri, sel).map {
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
