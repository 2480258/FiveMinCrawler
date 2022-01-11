package com.fivemin.core.engine.transaction.serialize.postParser

import arrow.core.none
import arrow.core.toOption
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.getSuccResponse
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsRequestReq
import com.fivemin.core.ElemIterator
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.ParserNavigator
import com.fivemin.core.engine.RequestType
import com.fivemin.core.engine.transaction.serialize.postParser.linkExtract.LinkParserImpl
import kotlinx.coroutines.runBlocking
import org.testng.Assert.*
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class LinkExtractImplTest {

    var link = LinkExtractImpl(LinkParserImpl())
    var uriIt = ElemIterator(UriIterator())

    @BeforeMethod
    fun setUp() {
        link = LinkExtractImpl(LinkParserImpl())
        uriIt = ElemIterator(UriIterator())
    }

    @AfterMethod
    fun tearDown() {
    }

    @Test
    fun hashReleative_ReturnsLink() {
        runBlocking {
            var resp = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a")
                .upgradeAsRequestReq().upgrade().getSuccResponse("<a href=\"/test1#tt\"></a>")

            var ret = link.extract(resp, LinkSelector(ParserNavigator("a:nth-child(1)"), none()).toOption())

            if (ret.isEmpty()) {
                throw IllegalArgumentException()
            }

            ret.map {
                assertEquals(it.count(), 1)
                assertEquals(it.first().absoluteURI.toString(), "http://" + uriIt[0]!!.host + "/test1")
                assertEquals(it.first().absoluteURI.isAbsolute, true)
            }
        }
    }

    @Test
    fun hashAbsolute_ReturnsLink() {
        runBlocking {
            var resp = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a")
                .upgradeAsRequestReq().upgrade().getSuccResponse("<a href=\"https://aaa.com/test1#tt\"></a>")

            var ret = link.extract(resp, LinkSelector(ParserNavigator("a:nth-child(1)"), none()).toOption())

            if (ret.isEmpty()) {
                throw IllegalArgumentException()
            }

            ret.map {
                assertEquals(it.count(), 1)
                assertEquals(it.first().absoluteURI.toString(), "https://aaa.com/test1")
                assertEquals(it.first().absoluteURI.isAbsolute, true)
            }
        }
    }

    @Test
    fun releative_ReturnsLink() {
        runBlocking {
            var resp = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a")
                .upgradeAsRequestReq().upgrade().getSuccResponse("<a href=\"/test1\"></a>")

            var ret = link.extract(resp, LinkSelector(ParserNavigator("a:nth-child(1)"), none()).toOption())

            if (ret.isEmpty()) {
                throw IllegalArgumentException()
            }

            ret.map {
                assertEquals(it.count(), 1)
                assertEquals(it.first().absoluteURI.toString(), "http://" + uriIt[0]!!.host + "/test1")
                assertEquals(it.first().absoluteURI.isAbsolute, true)
            }
        }
    }

    @Test
    fun absolute_ReturnsLink() {
        runBlocking {
            var resp = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a")
                .upgradeAsRequestReq().upgrade().getSuccResponse("<a href=\"https://aaa.com/test1\"></a>")

            var ret = link.extract(resp, LinkSelector(ParserNavigator("a:nth-child(1)"), none()).toOption())

            if (ret.isEmpty()) {
                throw IllegalArgumentException()
            }

            ret.map {
                assertEquals(it.count(), 1)
                assertEquals(it.first().absoluteURI.toString(), "https://aaa.com/test1")
                assertEquals(it.first().absoluteURI.isAbsolute, true)
            }
        }
    }

    @Test
    fun withoutRegex_ReturnsLink() {
        runBlocking {
            var resp = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a")
                .upgradeAsRequestReq().upgrade().getSuccResponse("<img src=\"https://aaa.com/test1\">")

            var ret = link.extract(resp, LinkSelector(ParserNavigator("*"), none()).toOption())

            if (ret.isEmpty()) {
                throw IllegalArgumentException()
            }

            ret.map {
                assertEquals(it.count(), 1)
                assertEquals(it.first().absoluteURI.toString(), "https://aaa.com/test1")
                assertEquals(it.first().absoluteURI.isAbsolute, true)
            }
        }
    }

    @Test
    fun srcLink_ReturnsLink() {

        runBlocking {
            var resp = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a")
                .upgradeAsRequestReq().upgrade().getSuccResponse("<img src=\"https://aaa.com/test1\">")

            var ret = link.extract(resp, LinkSelector(ParserNavigator("*"), Regex("http").toOption()).toOption())

            if (ret.isEmpty()) {
                throw IllegalArgumentException()
            }

            ret.map {
                assertEquals(it.count(), 1)
                assertEquals(it.first().absoluteURI.toString(), "https://aaa.com/test1")
                assertEquals(it.first().absoluteURI.isAbsolute, true)
            }
        }
    }

    @Test
    fun withoutCss_ReturnsLink() {

        runBlocking {
            var resp = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a")
                .upgradeAsRequestReq().upgrade().getSuccResponse("<a href=\"https://aaa.com/test1\"></a>")

            var ret = link.extract(resp, LinkSelector(ParserNavigator("*"), Regex("http").toOption()).toOption())

            if (ret.isEmpty()) {
                throw IllegalArgumentException()
            }

            ret.map {
                assertEquals(it.count(), 1)
                assertEquals(it.first().absoluteURI.toString(), "https://aaa.com/test1")
                assertEquals(it.first().absoluteURI.isAbsolute, true)
            }
        }
    }

    @Test
    fun regexReject_ReturnsLink() {

        runBlocking {
            var resp = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a")
                .upgradeAsRequestReq().upgrade().getSuccResponse("<a href=\"https://aaa.com/test1#tt\"></a>")

            var ret = link.extract(resp, LinkSelector(ParserNavigator("*"), Regex("test2").toOption()).toOption())

            if (ret.isEmpty()) {
                throw IllegalArgumentException()
            }

            ret.map {
                assertEquals(it.count(), 0)
            }
        }
    }

    @Test
    fun multipleSelectCss_ReturnsLink() {

        runBlocking {
            var resp = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a")
                .upgradeAsRequestReq().upgrade().getSuccResponse("<a href=\"/test1\"></a><a href=\"/test2\"></a>")

            var ret = link.extract(resp, LinkSelector(ParserNavigator("*"), Regex("http").toOption()).toOption())

            if (ret.isEmpty()) {
                throw IllegalArgumentException()
            }

            ret.map {
                assertEquals(it.count(), 2)
            }
        }
    }
}
