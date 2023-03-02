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

package com.fivemin.core.engine.transaction.export

import com.fivemin.core.LoggerController
import com.fivemin.core.engine.ExportHandle
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.SerializeTransaction

/**
 * Distributes Transactions to pages.
 */
interface ExportParser {
    fun <Document : Request> parse(trans: SerializeTransaction<Document>): Iterable<ExportHandle>
}

class ExportParserImpl(private val pages: Iterable<ExportPage>) : ExportParser {

    companion object {
        private val logger = LoggerController.getLogger("ExportParserImpl")
    }

    override fun <Document : Request> parse(trans: SerializeTransaction<Document>): Iterable<ExportHandle> {
        var pg = pages.filter {
            it.isAcceptable(trans)
        }

        if (!pg.any()) {
            throw NoMatchedExportPageException(trans.request)
        }

        return pg.flatMap {
            it.export(trans)
        }
    }
}

class NoMatchedExportPageException(request: Request): Exception("can't find matched export page with request")