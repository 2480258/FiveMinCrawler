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
import kotlinx.coroutines.runBlocking
import org.testng.Assert.*
import org.testng.annotations.Test

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
                pp.extract(creq, TaskMockFactory.createTaskInfo(), TaskMockFactory.createSessionStarted<Any>()).await()
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
                pp.extract(creq, TaskMockFactory.createTaskInfo(), TaskMockFactory.createSessionStarted<Any>()).await()
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
                pp.extract(creq, TaskMockFactory.createTaskInfo(), TaskMockFactory.createSessionStarted<Any>()).await()
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

            var ret = pp.extract(creq, TaskMockFactory.createTaskInfo(), mockk()).await().fold({
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

            var ret = pp.extract(creq, TaskMockFactory.createTaskInfo(), mockk()).await().fold({ fail() }) {
                assertEquals(it.count(), 0)
            }
        }
    }
}
