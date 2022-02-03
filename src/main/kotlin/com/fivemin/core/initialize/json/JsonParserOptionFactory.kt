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

package com.fivemin.core.initialize.json

import com.fivemin.core.engine.DirectIO
import com.fivemin.core.engine.transaction.prepareRequest.preParser.PreParserImpl
import com.fivemin.core.engine.transaction.serialize.postParser.PostParserImpl
import com.fivemin.core.initialize.ParseOption
import com.fivemin.core.initialize.RequesterFactory
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@kotlinx.serialization.Serializable
data class JsonOptionFormat(
    val requestFormat: JsonRequesterCompFormat,
    val parseFormat: JsonPrePostParserFormat,
    val exportFormat: JsonExportParserFormat
)

class JsonParserOptionFactory(
    val jsonString: String,
    val factory: List<RequesterFactory>,
    val io: DirectIO
) {
    val format: JsonOptionFormat
    val option: ParseOption
    init {
        format = Json.decodeFromString<JsonOptionFormat>(jsonString)
        option = getOption(factory)
    }

    private fun getOption(factories: Iterable<RequesterFactory>): ParseOption {
        return ParseOption(
            PreParserImpl(
                format.parseFormat.globalCondition.build(),
                format.parseFormat.pages.map {
                    it.buildPrePage()
                },
                format.parseFormat.attributeRequester.build()
            ),
            PostParserImpl(
                format.parseFormat.pages.map {
                    it.buildPostPage()
                }
            ),
            format.exportFormat.build(io),
            format.requestFormat.build(factories, io)
        )
    }
}
