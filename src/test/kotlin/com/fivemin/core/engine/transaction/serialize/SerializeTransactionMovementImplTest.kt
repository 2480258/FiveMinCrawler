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

package com.fivemin.core.engine.transaction.serialize

import arrow.core.Either
import com.fivemin.core.AttributeMockFactory
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.StubMockFactory
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.RequestType
import com.fivemin.core.engine.match
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.testng.annotations.Test
import java.net.URI
import kotlin.test.assertEquals

class SerializeTransactionMovementImplTest {
    
    fun mockPostParser(info: PostParseInfo): PostParser<Request> {
        val post: PostParser<Request> = mockk()
        
        coEvery {
            post.getPostParseInfo(any(), any(), any())
        } returns (Either.catch { info })
        
        return post
    }
    
    @Test
    fun testMove_SingleInte() {
        val serial = SerializeTransactionMovementImpl(
            mockPostParser(
                PostParseInfo(
                    listOf(
                        AttributeMockFactory.getSingleStringAttr(
                            "a", "a"
                        )
                    )
                )
            )
        )
        
        val info = StubMockFactory.mockInfo()
        val state = StubMockFactory.mockState()
        
        val doc =
            DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK).upgrade().upgradeAsDocument("a")
                .upgrade()
        
        val result = runBlocking {
            serial.move(doc, info, state).await()
        }
        
        result.fold({ throw it }, {
            assertEquals(it.attributes.count(), 1)
            assertEquals(it.attributes.first().item.count(), 1)
            it.attributes.first().item.first().match({ assertEquals(it.body, "a") }, { throw NullPointerException() })
            
            assert(it.tags.contains("a"))
            assertEquals(it.tags["a"].convertToAttribute, true)
            assertEquals(it.tags["a"].isAlias, false)
            assertEquals(it.tags["a"].isUnique, false)
            assertEquals(it.tags["a"].value, "a")
        })
    }
    
    
    @Test
    fun testMove_MultiInte() {
        val serial = SerializeTransactionMovementImpl(
            mockPostParser(
                PostParseInfo(
                    listOf(
                        AttributeMockFactory.getMultiStringAttr(
                            "a", listOf("a", "b", "c")
                        )
                    )
                )
            )
        )
        
        val info = StubMockFactory.mockInfo()
        val state = StubMockFactory.mockState()
        
        val doc =
            DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK).upgrade().upgradeAsDocument("a")
                .upgrade()
        
        val result = runBlocking {
            serial.move(doc, info, state).await()
        }
        
        result.fold({ throw it }, {
            assertEquals(it.attributes.count(), 1)
            assertEquals(it.attributes.first().item.count(), 3)
            it.attributes.first().item.first().match({ assertEquals(it.body, "a") }, { throw NullPointerException() })
            
            assert(it.tags.contains("a"))
            assertEquals(it.tags["a"].convertToAttribute, true)
            assertEquals(it.tags["a"].isAlias, false)
            assertEquals(it.tags["a"].isUnique, false)
            assertEquals(it.tags["a"].value, "a")
        })
    }
    
    @Test
    fun testMove_Ext() {
        val serial = SerializeTransactionMovementImpl(
            mockPostParser(
                PostParseInfo(
                    listOf(
                        AttributeMockFactory.getMultiStringAttr("a", listOf())
                    )
                )
            )
        )
        
        val info = StubMockFactory.mockInfo()
        val state = StubMockFactory.mockState()
        
        val doc =
            DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK).upgrade().upgradeAsDocument("a")
                .upgrade()
        
        val result = runBlocking {
            serial.move(doc, info, state).await()
        }
        
        result.fold({ throw it }, {
            assertEquals(it.attributes.count(), 1)
            assertEquals(it.attributes.first().item.count(), 0)
            
            assert(!it.tags.contains("a"))
        })
    }
}