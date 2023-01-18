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

import arrow.core.toOption
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.getHttpRequest
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.ElemIterator
import com.fivemin.core.TaskMockFactory
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.*
import io.mockk.*
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.testng.Assert.*
import org.testng.annotations.Test
import java.net.URI

class PostParserContentPageImplTest {
    val uriIt = ElemIterator(UriIterator())

    fun createInternal(it: Iterable<InternalContentInfo>): InternalContentInfoFactory<Request> {
        val fac: InternalContentInfoFactory<Request> = mockk()

        coEvery {
            fac.get(any())
        } returns (it.toOption())

        return fac
    }

    fun createFactory(it: Iterable<RequestLinkInfo>): RequestContentInfoFactory<Request> {
        val fac: RequestContentInfoFactory<Request> = mockk()

        coEvery {
            fac.get(any())
        } returns (RequestContentInfo(it))

        return fac
    }

    @Test
    fun testExtractInte() {

        runBlocking {
            var creq =
                DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a").upgrade()

            val pp = PostParserContentPageImpl(
                PageName("a"),
                createFactory(listOf()),
                createFactory(listOf()),
                createInternal(listOf(InternalContentInfo("a", listOf("a")))),
                DocumentAttributeFactoryImpl()
            )

            var ret =
                pp.extract(creq, TaskMockFactory.createSessionStarted<Any>()).await()
                    .fold({ fail() }) {
                        assertEquals(it.count(), 1)
                    }
        }
    }

    @Test
    fun testExtractExt() {

        runBlocking {
            var creq =
                DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a").upgrade()

            val pp = PostParserContentPageImpl(
                PageName("a"),
                createFactory(listOf()),
                createFactory(
                    listOf(
                        RequestLinkInfo(
                            "a",
                            listOf(getHttpRequest(uriIt.gen(), RequestType.LINK)),
                            InitialOption()
                        )
                    )
                ),
                createInternal(listOf()),
                DocumentAttributeFactoryImpl()
            )

            var ret =
                pp.extract(creq, TaskMockFactory.createSessionStarted<Any>(TaskMockFactory.createTaskInfo(policySet = TaskMockFactory.createPolicySet2()))).await()
                    .fold({ fail() }) {
                        assertEquals(it.count(), 1)
                    }
        }
    }

    @Test
    fun testExtractLink() {

        runBlocking {
            var creq =
                DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a").upgrade()

            val pp = PostParserContentPageImpl(
                PageName("a"),
                createFactory(
                    listOf(
                        RequestLinkInfo(
                            "a",
                            listOf(getHttpRequest(uriIt.gen(), RequestType.LINK)),
                            InitialOption()
                        )
                    )
                ),
                createFactory(listOf()),
                createInternal(listOf()),
                DocumentAttributeFactoryImpl()
            )

            var ret =
                pp.extract(creq, TaskMockFactory.createSessionStarted<Any>()).await()
                    .fold({
                        fail()
                    }) {
                        assertEquals(it.count(), 0)
                    }
        }
    }

    @Test
    fun testExtractNoPage() {

        runBlocking {
            var creq =
                DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a").upgrade()

            val pp = PostParserContentPageImpl(
                PageName("ab"),
                createFactory(listOf()),
                createFactory(listOf()),
                createInternal(listOf()),
                DocumentAttributeFactoryImpl()
            )

            var ret = pp.extract(creq, TaskMockFactory.createSessionStarted<Request>()).await().fold({
            }) {
                fail()
            }
        }
    }

    @Test
    fun testExtractNone() {

        runBlocking {
            var creq =
                DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a").upgrade()

            val pp = PostParserContentPageImpl(
                PageName("a"),
                createFactory(listOf()),
                createFactory(listOf()),
                createInternal(listOf()),
                DocumentAttributeFactoryImpl()
            )

            var ret = pp.extract(creq, TaskMockFactory.createSessionStarted<Request>()).await().fold({ fail() }) {
                assertEquals(it.count(), 0)
            }
        }
    }
}
