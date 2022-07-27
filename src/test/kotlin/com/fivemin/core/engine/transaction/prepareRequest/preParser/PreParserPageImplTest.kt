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
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.PageCondition
import com.fivemin.core.engine.transaction.PageConditionResult
import com.fivemin.core.engine.transaction.TagBuilder
import io.mockk.every
import io.mockk.mockk
import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.net.URI
import kotlin.test.assertNotEquals
import kotlin.test.fail

class PreParserPageImplTest {
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
    
    private fun mockTagBuilder() : TagBuilder<InitialTransaction<Request>, Request> {
        val tagBuilder : TagBuilder<InitialTransaction<Request>, Request> = mockk()
        
        every {
            tagBuilder.build(any())
        } returns (TagRepositoryImpl())
        
        return tagBuilder
    }
    
    private fun mockPageWithPreDefined(name: String) : InitialTransaction<Request> {
        val doc = DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK).upgrade()
        val initialOption = InitialOption(parseOption = Some(ParseOption(PageName(name))))
        every {
            doc.option
        } returns (initialOption)
        
        return doc
    }
    
    
    @Test
    fun testMakeTransaction_WithMatchedPreDefined_WithMatchedCondition() {
        val preParserPageImpl = PreParserPageImpl(PageName("aaa"), mockSuccPageCondition(), mockk(), mockk(), mockTagBuilder())
        
        assertNotEquals(preParserPageImpl.makeTransaction(mockPageWithPreDefined("aaa")), none())
    }
    
    @Test
    fun testMakeTransaction_WithMatchedPreDefined_WithNotMatchedCondition() {
        val preParserPageImpl = PreParserPageImpl(PageName("aaa"), mockFailPageCondition(), mockk(), mockk(), mockTagBuilder())
        
        assertNotEquals(preParserPageImpl.makeTransaction(mockPageWithPreDefined("aaa")), none())
    }
    
    @Test
    fun testMakeTransaction_WithNotMatchedPreDefined_WithMatchedCondition() {
        val preParserPageImpl = PreParserPageImpl(PageName("aaa"), mockSuccPageCondition(), mockk(), mockk(), mockTagBuilder())
        
        val result = preParserPageImpl.makeTransaction(mockPageWithPreDefined("aan"))
        
        if(result != none<PrepareTransaction<Request>>()) {
            fail()
        }
    }
    
    
    @Test
    fun testMakeTransaction_WithNotMatchedPreDefined_WithNotMatchedCondition() {
        val preParserPageImpl = PreParserPageImpl(PageName("aaa"), mockFailPageCondition(), mockk(), mockk(), mockTagBuilder())
        
        val result = preParserPageImpl.makeTransaction(mockPageWithPreDefined("aan"))
        
        assertEquals(result, none<PrepareTransaction<Request>>())
    }
}