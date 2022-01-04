package com.fivemin.core.engine.transaction.export

import com.fivemin.core.AttributeMockFactory
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.ElemIterator
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.RequestType
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.net.URI

class SpecialAttributeTagFactoryTest {

    lateinit var specialAttributeTagFactory: SpecialAttributeTagFactory
    var uriIt = ElemIterator(UriIterator())

    @BeforeMethod
    fun setUp() {
        specialAttributeTagFactory = SpecialAttributeTagFactory()
        uriIt = ElemIterator(UriIterator())
    }

    @Test
    fun testBuild() {
        val attr = AttributeMockFactory.getSingleStringAttr("a", "b")

        val req = DocumentMockFactory.getRequest(URI("http://aaaaa.com/abc/dfg/z"), RequestType.LINK).upgrade()
            .upgradeAsDocument("a").upgrade().upgrade(listOf(attr))

        val repo = specialAttributeTagFactory.build(req, AttributeLocator("a", 0))

        assertEquals(repo["lastseg"].value, "z")
        assertEquals(repo["inc"].value, "00")
        assertEquals(repo["ext"].value, "")
        assertEquals(repo["name"].value, "a")
    }
}
