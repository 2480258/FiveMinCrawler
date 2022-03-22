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
import com.fivemin.core.engine.session.bFilter.BloomFilterImpl
import com.fivemin.core.engine.transaction.StringUniqueKey
import io.mockk.every
import io.mockk.mockk
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

    lateinit var r: BloomFilterUniqueKeyRepository
    var it = ElemIterator(UniqueKeyIterator())

    @BeforeMethod
    fun before() {
        val mock: BloomFilterFactory = mockk()
        
        every {
            mock.createEmpty()
        } returns (BloomFilterImpl(100, 0.00000001))
        
        r = BloomFilterUniqueKeyRepository(mock, none())
        
        it = ElemIterator(UniqueKeyIterator())
    }

    @Test
    fun testAddAliasWithNetural() {
        r.addUniqueKey(it.gen())
        
        assertThrows {
            r.addUniqueKey(it[0]!!)
        }
    }
    
    @Test
    fun testAddAliasWithNeturalWithDetached() {
        r.addUniqueKeyWithDetachableThrows(it.gen())
        
        assertThrows {
            r.addUniqueKey(it[0]!!)
        }
    }
    
    @Test
    fun testAddAliasWithNeturalWithNotDetached() {
        r.addUniqueKeyWithNotDetachableThrows(it.gen())
        
        assertThrows {
            r.addUniqueKey(it[0]!!)
        }
    }
    
    @Test
    fun testAddAliasWithNotDetached() {
        r.addUniqueKey(it.gen())
        
        assertThrows {
            r.addUniqueKeyWithNotDetachableThrows(it[0]!!)
        }
    }
    
    @Test
    fun testAddAliasWithDetached() {
        r.addUniqueKey(it.gen())
        
        assertThrows {
            r.addUniqueKeyWithDetachableThrows(it[0]!!)
        }
    }
    
    @Test
    fun testConveyToDetached() {
        val token = r.addUniqueKey(it.gen())
        r.notifyMarkedDetachable(listOf(token))
        
        assertThrows {
            r.addUniqueKey(it[0]!!)
        }
    }
    
    
    @Test
    fun testConveyToNotDetached() {
        val token = r.addUniqueKey(it.gen())
        r.notifyMarkedNotDetachable(listOf(token))
        
        assertThrows {
            r.addUniqueKey(it[0]!!)
        }
    }
}