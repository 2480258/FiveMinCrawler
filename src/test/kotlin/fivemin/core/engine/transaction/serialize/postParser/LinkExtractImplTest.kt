package fivemin.core.engine.transaction.serialize.postParser

import arrow.core.none
import arrow.core.toOption
import fivemin.core.DocumentMockFactory
import fivemin.core.DocumentMockFactory.Companion.getSuccResponse
import fivemin.core.DocumentMockFactory.Companion.upgrade
import fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import fivemin.core.DocumentMockFactory.Companion.upgradeAsRequestReq
import fivemin.core.ElemIterator
import fivemin.core.UriIterator
import fivemin.core.engine.ParserNavigator
import fivemin.core.engine.RequestType
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import org.testng.Assert.*
import java.net.URI
import kotlin.test.asserter

class LinkExtractImplTest {

    var link = LinkExtractImpl()
    var uriIt = ElemIterator(UriIterator())

    @BeforeMethod
    fun setUp() {
        link = LinkExtractImpl()
        uriIt = ElemIterator(UriIterator())
    }

    @AfterMethod
    fun tearDown() {
    }


    @Test
    fun hashReleative_ReturnsLink() {
        var resp = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a")
            .upgradeAsRequestReq().upgrade().getSuccResponse("<a href=\"/test1#tt\"></a>")

        var ret = link.extract(resp, LinkSelector(ParserNavigator("a:nth-child(1)"), none()).toOption())

        if(ret.isEmpty()) {
            throw IllegalArgumentException()
        }

        ret.map {
            assertEquals(it.count(), 1)
            assertEquals(it.first().absoluteURI.toString(), "http://" + uriIt[0]!!.host + "/test1")
            assertEquals(it.first().absoluteURI.isAbsolute, true)
        }
    }

    @Test
    fun hashAbsolute_ReturnsLink() {
        var resp = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a")
            .upgradeAsRequestReq().upgrade().getSuccResponse("<a href=\"https://aaa.com/test1#tt\"></a>")

        var ret = link.extract(resp, LinkSelector(ParserNavigator("a:nth-child(1)"), none()).toOption())

        if(ret.isEmpty()) {
            throw IllegalArgumentException()
        }

        ret.map {
            assertEquals(it.count(), 1)
            assertEquals(it.first().absoluteURI.toString(), "https://aaa.com/test1")
            assertEquals(it.first().absoluteURI.isAbsolute, true)
        }
    }

    @Test
    fun releative_ReturnsLink() {
        var resp = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a")
            .upgradeAsRequestReq().upgrade().getSuccResponse("<a href=\"/test1\"></a>")

        var ret = link.extract(resp, LinkSelector(ParserNavigator("a:nth-child(1)"), none()).toOption())

        if(ret.isEmpty()) {
            throw IllegalArgumentException()
        }

        ret.map {
            assertEquals(it.count(), 1)
            assertEquals(it.first().absoluteURI.toString(), "http://" + uriIt[0]!!.host + "/test1")
            assertEquals(it.first().absoluteURI.isAbsolute, true)
        }
    }

    @Test
    fun absolute_ReturnsLink() {
        var resp = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a")
            .upgradeAsRequestReq().upgrade().getSuccResponse("<a href=\"https://aaa.com/test1\"></a>")

        var ret = link.extract(resp, LinkSelector(ParserNavigator("a:nth-child(1)"), none()).toOption())

        if(ret.isEmpty()) {
            throw IllegalArgumentException()
        }

        ret.map {
            assertEquals(it.count(), 1)
            assertEquals(it.first().absoluteURI.toString(), "https://aaa.com/test1")
            assertEquals(it.first().absoluteURI.isAbsolute, true)
        }
    }


    @Test
    fun withoutRegex_ReturnsLink() {
        var resp = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a")
            .upgradeAsRequestReq().upgrade().getSuccResponse("<img src=\"https://aaa.com/test1\">")

        var ret = link.extract(resp, LinkSelector(ParserNavigator("*"), none()).toOption())

        if(ret.isEmpty()) {
            throw IllegalArgumentException()
        }

        ret.map {
            assertEquals(it.count(), 1)
            assertEquals(it.first().absoluteURI.toString(), "https://aaa.com/test1")
            assertEquals(it.first().absoluteURI.isAbsolute, true)
        }
    }


    @Test
    fun srcLink_ReturnsLink() {
        var resp = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a")
            .upgradeAsRequestReq().upgrade().getSuccResponse("<img src=\"https://aaa.com/test1\">")

        var ret = link.extract(resp, LinkSelector(ParserNavigator("*"), Regex("http").toOption()).toOption())

        if(ret.isEmpty()) {
            throw IllegalArgumentException()
        }

        ret.map {
            assertEquals(it.count(), 1)
            assertEquals(it.first().absoluteURI.toString(), "https://aaa.com/test1")
            assertEquals(it.first().absoluteURI.isAbsolute, true)
        }
    }

    @Test
    fun withoutCss_ReturnsLink() {
        var resp = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a")
            .upgradeAsRequestReq().upgrade().getSuccResponse("<a href=\"https://aaa.com/test1\"></a>")

        var ret = link.extract(resp, LinkSelector(ParserNavigator("*"), Regex("http").toOption()).toOption())

        if(ret.isEmpty()) {
            throw IllegalArgumentException()
        }

        ret.map {
            assertEquals(it.count(), 1)
            assertEquals(it.first().absoluteURI.toString(), "https://aaa.com/test1")
            assertEquals(it.first().absoluteURI.isAbsolute, true)
        }
    }

    @Test
    fun regexReject_ReturnsLink() {
        var resp = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a")
            .upgradeAsRequestReq().upgrade().getSuccResponse("<a href=\"https://aaa.com/test1#tt\"></a>")

        var ret = link.extract(resp, LinkSelector(ParserNavigator("*"), Regex("test2").toOption()).toOption())

        if(ret.isEmpty()) {
            throw IllegalArgumentException()
        }

        ret.map {
            assertEquals(it.count(), 0)
        }
    }


    @Test
    fun multipleSelectCss_ReturnsLink() {
        var resp = DocumentMockFactory.getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("a")
            .upgradeAsRequestReq().upgrade().getSuccResponse("<a href=\"/test1\"></a><a href=\"/test2\"></a>")

        var ret = link.extract(resp, LinkSelector(ParserNavigator("*"), Regex("http").toOption()).toOption())

        if(ret.isEmpty()) {
            throw IllegalArgumentException()
        }

        ret.map {
            assertEquals(it.count(), 2)
        }
    }


}