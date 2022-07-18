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
import com.fivemin.core.engine.UniqueKey
import com.fivemin.core.engine.session.bFilter.BloomFilterImpl
import io.mockk.every
import io.mockk.mockk
import org.testng.Assert.assertThrows
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

class BloomFilterUniqueKeyRepositoryTest {
    
    lateinit var bf: BloomFilterUniqueKeyRepository
    lateinit var uniq: UniqueKeyIterator
    
    @BeforeMethod
    fun setUp() {
        val factory: BloomFilterFactory = mockk()
        every {
            factory.createEmpty()
        } returns (BloomFilterImpl(100, 0.000001))
        
        bf = BloomFilterUniqueKeyRepository(factory, none())
        uniq = UniqueKeyIterator()
    }
    
    @AfterMethod
    fun tearDown() {
    }

    
    @Test
    fun testAddUniqueKeyWithDetachableThrows_ThrowsWithDuplicatedKey() {
        val key = uniq.getNext()
        
        val token = bf.addUniqueKeyWithDetachableThrows(key)
        assertThrows {
            bf.addUniqueKeyWithDetachableThrows(key)
        }
    }
    
    @Test
    fun testAddUniqueKeyWithNotDetachableThrows_ThrowsWithDuplicatedKey() {
        val key = uniq.getNext()
        
        val token = bf.addUniqueKeyWithNotDetachableThrows(key)
        assertThrows {
            bf.addUniqueKeyWithNotDetachableThrows(key)
        }
    }
    
    @Test
    fun testAddUniqueKeyWithNotDetachableThrows_ThrowsWithCrossedDuplicatedToNonDuplicatedKey() {
        val key = uniq.getNext()
        
        val token = bf.addUniqueKeyWithNotDetachableThrows(key)
        assertThrows {
            bf.addUniqueKeyWithDetachableThrows(key)
        }
    }
    
    @Test
    fun testAddUniqueKeyWithNotDetachableThrows_ThrowsWithCrossedNonDuplicatedToDuplicatedKey() {
        val key = uniq.getNext()
        
        val token = bf.addUniqueKeyWithDetachableThrows(key)
        assertThrows {
            bf.addUniqueKeyWithNotDetachableThrows(key)
        }
    }
    
    @Test
    fun testAddUniqueKey_ThrowsWithDuplicatedKey() {
        val key = uniq.getNext()
        
        val token = bf.addUniqueKey(key)
        assertThrows {
            bf.addUniqueKey(key)
        }
    }
    
    
    //@Test - Too much time taken
    fun testMultiThreaded() {
        val threads = ConcurrentHashMap<Thread, Any>()
        
        val factory: BloomFilterFactory = mockk()
        every {
            factory.createEmpty()
        } returns (BloomFilterImpl(10000, 0.000001))
        
        val bff = BloomFilterUniqueKeyRepository(factory, none())
        
        for (i in 0 until 1000) {
            for (j in 0 until 10) {
                val t = Thread {
                    if ((i + j) % 4 == 0) addDetachable(bff, uniq.getNext())
                    if ((i + j) % 4 == 1) addNotDetachable(bff, uniq.getNext())
                    if ((i + j) % 4 == 2) addKeyAndSetDetachable(bff, uniq.getNext())
                    if ((i + j) % 4 == 3) addKeyAndSetNotDetachable(bff, uniq.getNext())
                }
                t.start()
                threads[t] = ""
            }
        }
        
        for (t in threads) {
            t.key.join()
        }
        
        println(bff.isTempStorageEmpty())
        assert(bff.isTempStorageEmpty())
    }
    
    fun addDetachable(bff: BloomFilterUniqueKeyRepository, key: UniqueKey) {
        //println(key.toString())
        bff.addUniqueKeyWithDetachableThrows(key)
        assert(bff.containsDetachable(key))
    }
    
    fun addNotDetachable(bff: BloomFilterUniqueKeyRepository, key: UniqueKey) {
        //println(key.toString())
        bff.addUniqueKeyWithNotDetachableThrows(key)
        assert(bff.containsNotDetachable(key))
    }
    
    fun addKeyAndSetDetachable(bff: BloomFilterUniqueKeyRepository, key: UniqueKey) {
        //println(key.toString())
        val token = bff.addUniqueKey(key)
        bff.notifyMarkedDetachable(listOf(token))
        assert(bff.containsDetachable(key))
    }
    
    fun addKeyAndSetNotDetachable(bff: BloomFilterUniqueKeyRepository, key: UniqueKey) {
        //println(key.toString())
        val token = bff.addUniqueKey(key)
        bff.notifyMarkedNotDetachable(listOf(token))
        assert(bff.containsNotDetachable(key))
    }
}