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

import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.getSuccResponse_Html
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsRequestDoc
import com.fivemin.core.engine.DocumentAttributeFactoryImpl
import com.fivemin.core.engine.ParserNavigator
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.RequestType
import com.fivemin.core.engine.parser.TextExtractorImpl
import kotlinx.coroutines.runBlocking
import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.net.URI

class InternalContentInfoFactoryImplTest {
    
    fun mockInternalContentParser(): InternalContentParser {
        return InternalContentParser("a", ParserNavigator("a"), TextSelectionMode.TEXT_CONTENT)
    }
    
    @Test
    fun testGet() {
        
        val responseData =
            DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK).upgrade().upgradeAsDocument("a")
                .upgradeAsRequestDoc().upgrade().getSuccResponse_Html("<a href=\"a.jpg\">test</a>")
        
        val doc =
            DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK).upgrade().upgradeAsDocument("a")
                .upgrade(responseData)
        
        
        val internal = InternalContentInfoFactoryImpl<Request>(
            listOf(mockInternalContentParser()), DocumentAttributeFactoryImpl(), TextExtractorImpl()
        )
        runBlocking {
            internal.get(doc).fold({ throw NullPointerException() }, {
                assertEquals(it.size, 1)
                assertEquals(it.first().attributeName, "a")
                assertEquals(it.first().data.size, 1)
                assertEquals(it.first().data[0], "test")
            })
        }
    }
}