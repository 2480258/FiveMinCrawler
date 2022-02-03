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

package com.fivemin.core.engine.session

import arrow.core.none
import arrow.core.toOption
import com.fivemin.core.ElemIterator
import com.fivemin.core.IteratorElemFactory
import com.fivemin.core.StringIterator
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.SessionToken
import com.fivemin.core.engine.UniqueKey
import com.fivemin.core.engine.transaction.StringUniqueKey
import org.testng.annotations.Test

import org.testng.Assert.*
import org.testng.annotations.BeforeMethod

class UniqueKeyIterator : IteratorElemFactory<UniqueKey> {
    val it = StringIterator()

    override fun getNext(): UniqueKey {
        return StringUniqueKey(it.getNext())
    }
}

class UniqueKeyRepositoryImplTest {

    lateinit var r: UniqueKeyRepositoryImpl
    var it = ElemIterator(UniqueKeyIterator())

    @BeforeMethod
    fun before() {
        it = ElemIterator(UniqueKeyIterator())
        r = UniqueKeyRepositoryImpl(none())
    }

    @Test
    fun testPreset() {
        var arch = ArchivedSessionSet(listOf(ArchivedSession(listOf(it.gen()))))
        r = UniqueKeyRepositoryImpl(arch.toOption())

        assertThrows {
            r.addAlias(SessionToken.create(), it[0]!!)
        }
    }

    @Test
    fun testAddAlias() {
        var fi = r.addAlias(SessionToken.create(), it.gen())
        assertThrows {
            var re = r.addAlias(SessionToken.create(), it[0]!!)
        }
    }


    @Test
    fun testMaxSelfAlias() {
        var src = SessionToken.create()

        r.addAlias(src, it.gen())
        r.addAlias(src, it[0]!!)
        r.addAlias(src, it[0]!!)

        assertThrows {
            r.addAlias(src, it[0]!!)
        }
    }


    @Test
    fun testTransferOwnership() {
        var src = SessionToken.create()
        var dest = SessionToken.create()

        r.addAlias(src, it.gen())
        r.transferOwnership(src, dest)
        r.addAlias(dest, it[0]!!)
    }
}