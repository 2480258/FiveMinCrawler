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
import com.fivemin.core.engine.UniqueKey
import com.fivemin.core.engine.session.bFilter.BloomFilterImpl
import com.fivemin.core.engine.session.database.DatabaseAdapterFactoryImpl
import io.mockk.every
import io.mockk.mockk
import org.testng.Assert.assertThrows
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class BloomFilterUniqueKeyRepositoryTest {
    
    lateinit var bf: CompositeUniqueKeyRepository
    lateinit var uniq: UniqueKeyIterator
    
    @BeforeMethod
    fun setUp() {
        val factory: BloomFilterFactory = mockk()
        every {
            factory.createEmpty()
        } returns (BloomFilterImpl(100, 0.000001))
        
        val persistFactory = DatabaseAdapterFactoryImpl("jdbc:sqlite::memory:")
        val persister = UniqueKeyPersisterImpl(persistFactory.get())
        
        bf = CompositeUniqueKeyRepository(
            persister,
            BloomFilterCache(factory),
            TemporaryUniqueKeyRepository(),
            UniqueKeyTokenFactory()
        )
        uniq = UniqueKeyIterator()
    }
    
    @AfterMethod
    fun tearDown() {
    }
    
    
    @Test
    fun testAddUniqueKeyWithDetachableThrows_ThrowsWithDuplicatedKey() {
        val key = uniq.getNext()
        
        val token = bf.lock_free_addUniqueKeyWithDetachableThrows(key)
        assertThrows {
            bf.lock_free_addUniqueKeyWithDetachableThrows(key)
        }
    }
    
    @Test
    fun testAddUniqueKeyWithNotDetachableThrows_ThrowsWithDuplicatedKey() {
        val key = uniq.getNext()
        
        val token = bf.lock_free_addUniqueKeyWithNotDetachableThrows(key)
        assertThrows {
            bf.lock_free_addUniqueKeyWithNotDetachableThrows(key)
        }
    }
    
    @Test
    fun testAddUniqueKeyWithNotDetachableThrows_ThrowsWithCrossedDuplicatedToNonDuplicatedKey() {
        val key = uniq.getNext()
        
        val token = bf.lock_free_addUniqueKeyWithNotDetachableThrows(key)
        assertThrows {
            bf.lock_free_addUniqueKeyWithDetachableThrows(key)
        }
    }
    
    @Test
    fun testAddUniqueKeyWithNotDetachableThrows_ThrowsWithCrossedNonDuplicatedToDuplicatedKey() {
        val key = uniq.getNext()
        
        val token = bf.lock_free_addUniqueKeyWithDetachableThrows(key)
        assertThrows {
            bf.lock_free_addUniqueKeyWithNotDetachableThrows(key)
        }
    }
    
    @Test
    fun testAddUniqueKey_ThrowsWithDuplicatedKey() {
        val key = uniq.getNext()
        
        val token = bf.lock_free_addUniqueKey(key)
        assertThrows {
            bf.lock_free_addUniqueKey(key)
        }
    }
    
    @Test
    fun testAddUniqueKey_ThrowsAfterPersister() {
        val factory: BloomFilterFactory = mockk()
        every {
            factory.createEmpty()
        } returns (BloomFilterImpl(100, 0.000001))
        
        
        val persistFactory = DatabaseAdapterFactoryImpl("jdbc:sqlite::memory:")
        val persister = UniqueKeyPersisterImpl(persistFactory.get())
        
        val bff = CompositeUniqueKeyRepository(
            persister,
            BloomFilterCache(factory),
            TemporaryUniqueKeyRepository(),
            UniqueKeyTokenFactory()
        )
        
        val key = uniq.getNext()
        
        persister.persistKey(key)
        
        assertThrows {
            bff.lock_free_addUniqueKey(key)
        }
    }
    
    @Test
    fun testAddUniqueKeyWithNonDetachable_ThrowsAfterPersister() {
        val factory: BloomFilterFactory = mockk()
        every {
            factory.createEmpty()
        } returns (BloomFilterImpl(100, 0.000001))
        
        
        val persistFactory = DatabaseAdapterFactoryImpl("jdbc:sqlite::memory:")
        val persister = UniqueKeyPersisterImpl(persistFactory.get())
        
        val bff = CompositeUniqueKeyRepository(
            persister,
            BloomFilterCache(factory),
            TemporaryUniqueKeyRepository(),
            UniqueKeyTokenFactory()
        )
        
        val key = uniq.getNext()
        
        persister.persistKey(key)
        
        assertThrows {
            bff.lock_free_addUniqueKeyWithNotDetachableThrows(key)
        }
    }
    
    @Test
    fun testMultiThreaded() {
        try {
            File("./sample.db").deleteRecursively()
            
            val threads = ConcurrentHashMap<Thread, Any>()
            
            val factory: BloomFilterFactory = mockk()
            every {
                factory.createEmpty()
            } returns (BloomFilterImpl(1000, 0.000001))
            
            val persistFactory = DatabaseAdapterFactoryImpl("jdbc:sqlite:sample.db")
            val persister = UniqueKeyPersisterImpl(persistFactory.get())
            
            val bff = CompositeUniqueKeyRepository(
                persister,
                BloomFilterCache(factory),
                TemporaryUniqueKeyRepository(),
                UniqueKeyTokenFactory()
            )
            
            val elemIterator = ElemIterator<UniqueKey>(UniqueKeyIterator())
            
            (0 until 1200).forEach {
                elemIterator.gen()
            }
            
            for (i in 0 until 300) {
                for (j in 0 until 4) {
                    val t = Thread {
                        if (j == 0) addDetachable(bff, elemIterator[i]!!)
                        if (j == 1) addNotDetachable(bff, elemIterator[i + 300]!!)
                        if (j == 2) addKeyAndSetDetachable(bff, elemIterator[i + 600]!!)
                        if (j == 3) addKeyAndSetNotDetachable(bff, elemIterator[i + 900]!!)
                    }
                    t.start()
                    threads[t] = ""
                }
            }
            
            for (t in threads) {
                t.key.join()
            }
            
            assert(bff.isTempStorageEmpty())
            
            for (i in 0 until 300) {
                assert(bff.containsDetachable(elemIterator[i]!!))
            }
            
            for (i in 300 until 600) {
                assert(bff.containsNotDetachableAndAdd(elemIterator[i]!!))
            }
            
            for (i in 600 until 900) {
                assert(bff.containsDetachable(elemIterator[i]!!))
            }
            
            for (i in 900 until 1200) {
                assert(bff.containsNotDetachableAndAdd(elemIterator[i]!!))
            }
            
        } finally {
            File("./sample.db").deleteRecursively()
        }
        
    }
    
    
    fun addDetachable(bff: CompositeUniqueKeyRepository, key: UniqueKey) {
        //println(key.toString())
        bff.lock_free_addUniqueKeyWithDetachableThrows(key)
        assert(bff.containsDetachable(key))
    }
    
    fun addNotDetachable(bff: CompositeUniqueKeyRepository, key: UniqueKey) {
        //println(key.toString())
        bff.lock_free_addUniqueKeyWithNotDetachableThrows(key)
        assert(bff.containsNotDetachableAndAdd(key))
    }
    
    fun addKeyAndSetDetachable(bff: CompositeUniqueKeyRepository, key: UniqueKey) {
        //println(key.toString())
        val token = bff.lock_free_addUniqueKey(key)
        bff.lock_free_notifyMarkedDetachable(listOf(token))
        assert(bff.containsDetachable(key))
    }
    
    fun addKeyAndSetNotDetachable(bff: CompositeUniqueKeyRepository, key: UniqueKey) {
        //println(key.toString())
        val token = bff.lock_free_addUniqueKey(key)
        bff.lock_free_notifyMarkedNotDetachable(listOf(token))
        assert(bff.containsNotDetachableAndAdd(key))
    }
}