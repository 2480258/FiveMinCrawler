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

import com.fivemin.core.ElemIterator
import com.fivemin.core.IteratorElemFactory
import com.fivemin.core.StringIterator
import com.fivemin.core.engine.UniqueKey
import com.fivemin.core.engine.session.bFilter.BloomFilterImpl
import com.fivemin.core.engine.session.database.DatabaseAdapterFactoryImpl
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

    lateinit var r: CompositeUniqueKeyRepository
    var it = ElemIterator(UniqueKeyIterator())

    @BeforeMethod
    fun before() {
        val mock: BloomFilterFactory = mockk()
        
        every {
            mock.createEmpty()
        } returns (BloomFilterImpl(100, 0.00000001))
    
        val persistFactory = DatabaseAdapterFactoryImpl("jdbc:sqlite::memory:")
        val persister = UniqueKeyPersisterImpl(persistFactory.get())
        
        r = CompositeUniqueKeyRepository(persister, BloomFilterCache(mock), TemporaryUniqueKeyRepository(), UniqueKeyTokenFactory())
        
        it = ElemIterator(UniqueKeyIterator())
    }

    @Test
    fun testAddAliasWithNetural() {
        r.lock_free_addUniqueKey(it.gen())
        
        assertThrows {
            r.lock_free_addUniqueKey(it[0]!!)
        }
    }
    
    @Test
    fun testAddAliasWithNeturalWithDetached() {
        r.lock_free_addUniqueKeyWithDetachableThrows(it.gen())
        
        assertThrows {
            r.lock_free_addUniqueKey(it[0]!!)
        }
    }
    
    @Test
    fun testAddAliasWithNeturalWithNotDetached() {
        r.lock_free_addUniqueKeyWithNotDetachableThrows(it.gen())
        
        assertThrows {
            r.lock_free_addUniqueKey(it[0]!!)
        }
    }
    
    @Test
    fun testAddAliasWithNotDetached() {
        r.lock_free_addUniqueKey(it.gen())
        
        assertThrows {
            r.lock_free_addUniqueKeyWithNotDetachableThrows(it[0]!!)
        }
    }
    
    @Test
    fun testAddAliasWithDetached() {
        r.lock_free_addUniqueKey(it.gen())
        
        assertThrows {
            r.lock_free_addUniqueKeyWithDetachableThrows(it[0]!!)
        }
    }
    
    @Test
    fun testConveyToDetached() {
        val token = r.lock_free_addUniqueKey(it.gen())
        r.lock_free_notifyMarkedDetachable(listOf(token))
        
        assertThrows {
            r.lock_free_addUniqueKey(it[0]!!)
        }
    }
    
    
    @Test
    fun testConveyToNotDetached() {
        val token = r.lock_free_addUniqueKey(it.gen())
        r.lock_free_notifyMarkedNotDetachable(listOf(token))
        
        assertThrows {
            r.lock_free_addUniqueKey(it[0]!!)
        }
    }
}