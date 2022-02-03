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
