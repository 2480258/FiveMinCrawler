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

package com.fivemin.core.engine.parser

import arrow.core.*
import com.fivemin.core.engine.HtmlElement
import com.fivemin.core.engine.HtmlMemoryData
import com.fivemin.core.engine.ParserNavigator
import com.fivemin.core.engine.transaction.serialize.postParser.TextExtractor
import com.fivemin.core.engine.transaction.serialize.postParser.TextSelectionMode

class TextExtractorImpl : TextExtractor {
    
    /**
     * Extract Text by ParserNavigator from given HtmlMemoryData with TextSelectionMode
     * Returns parsed text result.
     */
    override fun parse(htmlMemoryData: HtmlMemoryData, nav: ParserNavigator, mode: TextSelectionMode): Iterable<String> {
        val ret = htmlMemoryData.parseAsHtmlDocument {
            it.getElements(nav).map {
                it
            }
        }.fold({ listOf() }, { x -> x }).map {
            it.toOption()
        }.filterOption()

        return ret.map {
            selectMode(it, mode)
        }
    }

    private fun selectMode(elem: HtmlElement, mode: TextSelectionMode): String {
        return when (mode) {
            TextSelectionMode.TEXT_CONTENT -> elem.textContent
            TextSelectionMode.OUTER_HTML -> elem.outerHtml
            TextSelectionMode.INNER_HTML -> elem.innerHtml
        }
    }
}
