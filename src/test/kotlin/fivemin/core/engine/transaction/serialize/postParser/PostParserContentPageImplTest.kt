package fivemin.core.engine.transaction.serialize.postParser

import arrow.core.Option
import arrow.core.none
import arrow.core.toOption
import fivemin.core.DocumentMockFactory
import fivemin.core.DocumentMockFactory.Companion.getHttpRequest
import fivemin.core.DocumentMockFactory.Companion.upgrade
import fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import fivemin.core.ElemIterator
import fivemin.core.TaskMockFactory
import fivemin.core.UriIterator
import fivemin.core.engine.*
import fivemin.core.engine.session.SessionRepositoryImpl
import fivemin.core.export.ConfigControllerImpl
import fivemin.core.parser.HtmlDocumentFactoryImpl
import fivemin.core.request.HttpResponseMessage
import fivemin.core.request.MemoryFilterFactoryImpl
import fivemin.core.request.RequestHeaderProfile
import fivemin.core.request.adapter.RequesterAdapterImpl
import fivemin.core.request.adapter.ResponseAdapterImpl
import fivemin.core.request.cookie.CustomCookieJar
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.testng.annotations.Test

import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
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

        every {
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

            var ret = pp.extract(creq, TaskMockFactory.createTaskInfo(), TaskMockFactory.createSessionStarted<Any>()).await()

            assertEquals(ret.count(), 1)
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
                createFactory(listOf(RequestLinkInfo("a", listOf(getHttpRequest(uriIt.gen(), RequestType.LINK)), InitialOption()))),
                createInternal(listOf()),
                DocumentAttributeFactoryImpl()
            )

            var ret = pp.extract(creq, TaskMockFactory.createTaskInfo(), TaskMockFactory.createSessionStarted<Any>()).await()

            assertEquals(ret.count(), 1)
        }
    }

    @Test
    fun testExtractLink() {

        runBlocking {
            var creq =
                DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a").upgrade()

            val pp = PostParserContentPageImpl(
                PageName("a"),
                createFactory(listOf(RequestLinkInfo("a", listOf(getHttpRequest(uriIt.gen(), RequestType.LINK)), InitialOption()))),
                createFactory(listOf()),
                createInternal(listOf()),
                DocumentAttributeFactoryImpl()
            )

            var ret = pp.extract(creq, TaskMockFactory.createTaskInfo(), TaskMockFactory.createSessionStarted<Any>()).await()

            assertEquals(ret.count(), 0)
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

            var ret = pp.extract(creq, TaskMockFactory.createTaskInfo(), mockk()).await()

            assertEquals(ret.count(), 0)
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

            var ret = pp.extract(creq, TaskMockFactory.createTaskInfo(), mockk()).await()

            assertEquals(ret.count(), 0)
        }
    }
}