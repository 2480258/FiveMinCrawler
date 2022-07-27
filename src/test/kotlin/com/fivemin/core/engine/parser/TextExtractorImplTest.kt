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

import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.getSuccResponse_Html
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsRequestReq
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.serialize.postParser.TextSelectionMode
import org.testng.annotations.Test
import java.io.InvalidClassException

import java.net.URI
import kotlin.test.DefaultAsserter.assertEquals

class TextExtractorImplTest {
    
    @Test
    fun testParse() {
        DocumentMockFactory.getHttpRequest(URI("https://aa.com"), RequestType.LINK).upgrade()
            .upgradeAsDocument("a", WorkingSetMode.Enabled).upgradeAsRequestReq().upgrade().getSuccResponse_Html(
                "<html>\n" + "<body>\n" + "<div><p>asdf</p></div>\n" + "<a href=\"/users\">users</a> <a href=\"/about\">about</a><a href=\"/redirect\">redirect</a></body>\n" + "</html>"
            ).responseBody.ifSucc({ successBody ->
                successBody.body.ifHtml({
                    val text = TextExtractorImpl()
                    val result = text.parse(it, ParserNavigator("div"), TextSelectionMode.TEXT_CONTENT)
                    
                    assertEquals("parseMessage", "asdf", result.first())
                    
                }, {
                    throw Exception()
                })
            }, { throw Exception() })
    }
}