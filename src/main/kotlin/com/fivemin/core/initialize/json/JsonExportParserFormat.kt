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
import com.fivemin.core.engine.transaction.export.*

@kotlinx.serialization.Serializable
data class JsonExportParserFormat(
    val bookName: String,
    val pages: List<JsonExportPageFormat>
) {
    fun build(io: DirectIO): ExportParser {
        return ExportParserImpl(
            pages.map {
                it.build(io, bookName)
            }
        )
    }
}

@kotlinx.serialization.Serializable
data class JsonExportPageFormat(
    val pageName: String,
    val targetAttributeName: List<String>,
    val adapter: JsonExportAdapterFormat
) {
    fun build(io: DirectIO, bookName: String): ExportPage {
        return ExportPageImpl(pageName, targetAttributeName, adapter.build(io, bookName))
    }
}

@kotlinx.serialization.Serializable
data class JsonExportAdapterFormat(
    val mode: String,
    val fileNameTagExp: String
) {

    val JSON_ADAPTER = "Json"
    val BIN_ADAPTER = "Binary"

    fun build(io: DirectIO, bookName: String): ExportAdapter {
        var factory = ExportHandleFactoryImpl(io, bookName)

        if (mode == JSON_ADAPTER) {
            return JsonExportAdapter(TagExpression(fileNameTagExp), factory)
        }
        if (mode == BIN_ADAPTER) {
            return BinaryExportAdapter(TagExpression(fileNameTagExp), factory)
        } else {
            throw IllegalArgumentException()
        }
    }
}
