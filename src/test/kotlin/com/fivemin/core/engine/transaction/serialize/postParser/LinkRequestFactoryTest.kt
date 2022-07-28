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

import arrow.core.Some
import arrow.core.none
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.getSuccResponse_Html
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsRequestDoc
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.serialize.postParser.linkExtract.LinkParserImpl
import com.fivemin.core.request.RequestHeaderProfile
import kotlinx.coroutines.runBlocking
import org.testng.annotations.Test

import org.testng.Assert.*
import java.net.URI

class LinkRequestFactoryTest {
    
    @Test
    fun testGet_Without_PreDest() {
    
        val linkSelector = LinkSelector(ParserNavigator("*"), none())
        val linkParser = LinkParserImpl()
    
        val factory = LinkRequestFactory("a", linkSelector, none(), linkParser)
    
        val responseData = DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK)
            .upgrade()
            .upgradeAsDocument("a")
            .upgradeAsRequestDoc().upgrade().getSuccResponse_Html("<a href=\"a.jpg\">test</a>")
    
        val doc = DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK)
            .upgrade()
            .upgradeAsDocument("a")
            .upgrade(responseData)
    
        val result = runBlocking {
            factory.get(doc)
        }.fold({ throw NullPointerException() }, {
            assertEquals(it.name, "a")
            assertEquals(it.option.parseOption, none<ParseOption>())
            assertEquals(it.option.containerOption, none<ContainerOption>())
            assertEquals(it.option.requestOption, none<RequestOption>())
        
            assertEquals(it.requests.count(), 1)
            assertEquals(it.requests.first().parent, Some(doc.request.token))
            assertEquals(it.requests.first().requestType, RequestType.LINK)
            assertEquals(it.requests.first().target, URI("https://aaa.com/a.jpg"))
            assertEquals(it.requests.first().documentType, DocumentType.NATIVE_HTTP)
            assertEquals(it.requests.first().headerOption.referrer, Some(URI("https://aaa.com")))
            assertEquals(
                it.requests.first().headerOption.accept,
                Some("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.5")
            )
        
            assertEquals(it.requests.first().headerOption.requestHeaderProfile, none<RequestHeaderProfile>())
        })
    }
    
    @Test
    fun testGet_With_PreDest() {
        
        val linkSelector = LinkSelector(ParserNavigator("*"), none())
        val linkParser = LinkParserImpl()
        
        val factory = LinkRequestFactory("a", linkSelector, Some(PageName("b")), linkParser)
        
        val responseData = DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK)
            .upgrade()
            .upgradeAsDocument("a")
            .upgradeAsRequestDoc().upgrade().getSuccResponse_Html("<a href=\"a.jpg\">test</a>")
        
        val doc = DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK)
            .upgrade()
            .upgradeAsDocument("a")
            .upgrade(responseData)
        
        val result = runBlocking {
            factory.get(doc)
        }.fold({ throw NullPointerException() }, {
            assertEquals(it.name, "a")
            
            it.option.parseOption.fold({throw NullPointerException()} , {
                assertEquals(it.name, PageName("b"))
            })
            
            assertEquals(it.option.containerOption, none<ContainerOption>())
            assertEquals(it.option.requestOption, none<RequestOption>())
            
            assertEquals(it.requests.count(), 1)
            assertEquals(it.requests.first().parent, Some(doc.request.token))
            assertEquals(it.requests.first().requestType, RequestType.LINK)
            assertEquals(it.requests.first().target, URI("https://aaa.com/a.jpg"))
            assertEquals(it.requests.first().documentType, DocumentType.NATIVE_HTTP)
            assertEquals(it.requests.first().headerOption.referrer, Some(URI("https://aaa.com")))
            assertEquals(
                it.requests.first().headerOption.accept,
                Some("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.5")
            )
            
            assertEquals(it.requests.first().headerOption.requestHeaderProfile, none<RequestHeaderProfile>())
        })
    }
}