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

package com.fivemin.core.engine.transaction.prepareRequest.preParser

import arrow.core.Some
import arrow.core.none
import arrow.core.toOption
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.ElemIterator
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.PageCondition
import com.fivemin.core.engine.transaction.PageConditionResult
import io.mockk.every
import io.mockk.mockk
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.net.URI

class PreParserImplTest {

    lateinit var preParserImpl: PreParserImpl
    lateinit var trueCondition: PageCondition<InitialTransaction<Request>, Request>
    lateinit var falseCondition: PageCondition<InitialTransaction<Request>, Request>

    lateinit var truePage: PreParserPageImpl
    lateinit var falsePage: PreParserPageImpl

    val uriIt = ElemIterator(UriIterator())

    @BeforeMethod
    fun before() {
        trueCondition = mockk()

        every {
            trueCondition.check(any())
        } returns (PageConditionResult(true))

        falseCondition = mockk()

        every {
            falseCondition.check(any())
        } returns (PageConditionResult(false))

        truePage = mockk()

        every {
            truePage.makeTransaction<Request>(any())
        } answers {
            val req = firstArg<InitialTransaction<Request>>()

            req.upgradeAsDocument("true").toOption()
        }

        every {
            truePage.name
        } returns (PageName("truePage"))

        falsePage = mockk()

        every {
            falsePage.makeTransaction<Request>(any())
        } answers {
            none()
        }

        every {
            falsePage.name
        } returns (PageName("falsePage"))
    }

    fun generate(cond: PageCondition<InitialTransaction<Request>, Request>, it: List<PreParserPage>): PreParserImpl {
        return PreParserImpl(
            cond, it,
            RequestOption(
                RequesterPreference(
                    RequesterEngineInfo("false"), none()
                )
            )
        )
    }
    
    
    fun mockSuccPage(): PreParserPageImpl {
        val page: PreParserPageImpl = mockk()
        every {
            page.makeTransaction<Request>(any())
        } returns (Some(
            DocumentMockFactory.getRequest(URI("http://aaa.com"), RequestType.LINK).upgrade().upgradeAsDocument("a")
        ))
        every {
            page.name
        } returns (PageName("SuccPage"))
        return page
    }
    
    fun mockFailPage(): PreParserPageImpl {
        val page: PreParserPageImpl = mockk()
        every {
            page.makeTransaction<Request>(any())
        } returns (none())
        
        every {
            page.name
        } returns (PageName("SuccPage"))
        
        return page
    }
    
    @Test
    fun testMakeTransaction_Global_Not_Met() {
        val globalCondition = mockFailPageCondition()
        
        val preparser = PreParserImpl(globalCondition, listOf(mockSuccPage(), mockFailPage()), mockk())
        
        val doc = DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK)
            .upgrade()
        
        assertEquals(preparser.generateInfo(doc), none<PrepareTransaction<Request>>())
    }
    
    @Test
    fun testMakeTransaction_Global_Met_Page_Not_Met() {
        val globalCondition = mockSuccPageCondition()
        
        val preparser = PreParserImpl(globalCondition, listOf(mockFailPage()), mockk())
        
        val doc = DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK)
            .upgrade()
        
        assertThrows {preparser.generateInfo(doc) }
    }
    
    @Test
    fun testMakeTransaction_Global_Met_Page_Met_Twice() {
        val globalCondition = mockSuccPageCondition()
        
        val preparser = PreParserImpl(globalCondition, listOf(mockSuccPage(), mockSuccPage()), mockk())
        
        val doc = DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK)
            .upgrade()
        
        assertThrows {preparser.generateInfo(doc) }
    }
    
    
    @Test
    fun testMakeTransaction_Global_Met_Page_Met() {
        val globalCondition = mockSuccPageCondition()
        
        val preparser = PreParserImpl(globalCondition, listOf(mockSuccPage()), mockk())
        
        val doc = DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK)
            .upgrade()
    
        kotlin.test.assertNotEquals(preparser.generateInfo(doc), none<PrepareTransaction<Request>>())
    }
    
    
    @Test
    fun testMakeTransaction_Global_Met_Attr() {
        val globalCondition = mockSuccPageCondition()
        
        val preparser = PreParserImpl(globalCondition, listOf(mockFailPage()), mockk())
        
        val doc = DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.ATTRIBUTE)
            .upgrade()
    
        kotlin.test.assertNotEquals(preparser.generateInfo(doc), none<PrepareTransaction<Request>>())
    }
    
    @Test
    fun testMakeTransaction_Global_Met_Page_Met_Attr() {
        val globalCondition = mockSuccPageCondition()
        
        val preparser = PreParserImpl(globalCondition, listOf(mockSuccPage()), mockk())
        
        val doc = DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.ATTRIBUTE)
            .upgrade()
    
        kotlin.test.assertNotEquals(preparser.generateInfo(doc), none<PrepareTransaction<Request>>())
    }
    
    private fun mockSuccPageCondition(): PageCondition<InitialTransaction<Request>, Request> {
        val globalCondition: PageCondition<InitialTransaction<Request>, Request> = mockk(relaxed = true)
        every {
            globalCondition.check(any())
        } returns (PageConditionResult(true))
        return globalCondition
    }
    
    private fun mockFailPageCondition(): PageCondition<InitialTransaction<Request>, Request> {
        val globalCondition: PageCondition<InitialTransaction<Request>, Request> = mockk(relaxed = true)
        every {
            globalCondition.check(any())
        } returns (PageConditionResult(false))
        return globalCondition
    }

    @Test
    fun testDouble() {

        val req = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK)

        preParserImpl = generate(trueCondition, listOf(truePage, truePage))

        assertThrows {
            preParserImpl.generateInfo(req.upgrade())
        }
    }

    @Test
    fun testNone() {

        val req = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK)

        preParserImpl = generate(trueCondition, listOf(falsePage, falsePage))

        assertThrows {
            preParserImpl.generateInfo(req.upgrade())
        }

    }

    @Test
    fun testSingle() {
        val req = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK)

        preParserImpl = generate(trueCondition, listOf(truePage, falsePage))

        val prep = preParserImpl.generateInfo(req.upgrade())

        prep.fold({
            fail()
        }) {
            it.ifDocument({
                assertEquals(it.parseOption.name.name, "true")
            }, { fail() })
        }
    }
    
    
}
