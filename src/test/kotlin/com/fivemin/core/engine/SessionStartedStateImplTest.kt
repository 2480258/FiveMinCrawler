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

package com.fivemin.core.engine

import arrow.core.Either
import arrow.core.none
import com.fivemin.core.TaskMockFactory
import com.fivemin.core.engine.session.*
import com.fivemin.core.engine.session.bFilter.BloomFilterImpl
import com.fivemin.core.engine.session.database.DatabaseAdapterFactoryImpl
import com.fivemin.core.engine.transaction.StringUniqueKey
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.testng.Assert.assertThrows
import org.testng.annotations.Test

class SessionStartedStateImplTest {
    
    @Test
    fun testAddAlias_NotFinalize_EitherLeft() {
        val mock: BloomFilterFactory = mockk()
        
        every {
            mock.createEmpty()
        } returns (BloomFilterImpl(100, 0.00000001))
        
        val persistFactory = DatabaseAdapterFactoryImpl("jdbc:sqlite::memory:")
        val persister = UniqueKeyPersisterImpl(persistFactory.get())
        
        var keyRepo = spyk(CompositeUniqueKeyRepository(
            persister, BloomFilterCache(mock), TemporaryUniqueKeyRepository(), UniqueKeyTokenFactory()
        ))
        val fin = FinishObserverImpl()
        val sessRepo = SessionRepositoryImpl(keyRepo, FinishObserverImpl())
        
        val sess: SessionDetachableStartedStateImpl = spyk(
            SessionDetachableStartedStateImpl(
                SessionInfo(fin, keyRepo),
                SessionData(keyRepo, sessRepo),
                SessionContext(LocalUniqueKeyTokenRepo(), none()),
                TaskMockFactory.createTaskInfo()
            )
        )
    
    
        runBlocking {
            sess.addAlias(StringUniqueKey("a"), { Either.Left(IllegalArgumentException()) })
        }
        
        verify(exactly = 0) {
            keyRepo.lock_free_finalizeUniqueKey(any())
        }
    }
    
    @Test
    fun testAddAlias_NotFinalize_WhenThrows() {
        val mock: BloomFilterFactory = mockk()
    
        every {
            mock.createEmpty()
        } returns (BloomFilterImpl(100, 0.00000001))
    
        val persistFactory = DatabaseAdapterFactoryImpl("jdbc:sqlite::memory:")
        val persister = UniqueKeyPersisterImpl(persistFactory.get())
    
        var keyRepo = spyk(CompositeUniqueKeyRepository(
            persister, BloomFilterCache(mock), TemporaryUniqueKeyRepository(), UniqueKeyTokenFactory()
        ))
        val fin = FinishObserverImpl()
        val sessRepo = SessionRepositoryImpl(keyRepo, FinishObserverImpl())
    
        val sess: SessionDetachableStartedStateImpl = spyk(
            SessionDetachableStartedStateImpl(
                SessionInfo(fin, keyRepo),
                SessionData(keyRepo, sessRepo),
                SessionContext(LocalUniqueKeyTokenRepo(), none()),
                TaskMockFactory.createTaskInfo()
            )
        )
    
        assertThrows {
            runBlocking {
                async {
                    sess.addAlias<Int>(StringUniqueKey("a"), { throw IllegalAccessException() })
                }
            }
        }
    
        verify(exactly = 0) {
            keyRepo.lock_free_finalizeUniqueKey(any())
        }
    }
    
    
    @Test
    fun testAddAlias_Finalize() {
        val mock: BloomFilterFactory = mockk()
    
        every {
            mock.createEmpty()
        } returns (BloomFilterImpl(100, 0.00000001))
    
        val persistFactory = DatabaseAdapterFactoryImpl("jdbc:sqlite::memory:")
        val persister = UniqueKeyPersisterImpl(persistFactory.get())
    
        var keyRepo = spyk(CompositeUniqueKeyRepository(
            persister, BloomFilterCache(mock), TemporaryUniqueKeyRepository(), UniqueKeyTokenFactory()
        ))
        val fin = FinishObserverImpl()
        val sessRepo = SessionRepositoryImpl(keyRepo, FinishObserverImpl())
    
        val sess: SessionDetachableStartedStateImpl = spyk(
            SessionDetachableStartedStateImpl(
                SessionInfo(fin, keyRepo),
                SessionData(keyRepo, sessRepo),
                SessionContext(LocalUniqueKeyTokenRepo(), none()),
                TaskMockFactory.createTaskInfo()
            )
        )
    
        runBlocking {
            sess.addAlias(StringUniqueKey("a"), { Either.catch { } })
        }
        
        verify(exactly = 1) {
            keyRepo.lock_free_finalizeUniqueKey(any())
        }
    }
    
    @Test
    fun testAddAlias_Throws() {
        val mock: BloomFilterFactory = mockk()
        
        every {
            mock.createEmpty()
        } returns (BloomFilterImpl(100, 0.00000001))
        
        val persistFactory = DatabaseAdapterFactoryImpl("jdbc:sqlite::memory:")
        val persister = UniqueKeyPersisterImpl(persistFactory.get())
        
        var keyRepo = CompositeUniqueKeyRepository(
            persister, BloomFilterCache(mock), TemporaryUniqueKeyRepository(), UniqueKeyTokenFactory()
        )
        val fin = FinishObserverImpl()
        val sessRepo = SessionRepositoryImpl(keyRepo, FinishObserverImpl())
        
        val sess: SessionDetachableStartedStateImpl = spyk(
            SessionDetachableStartedStateImpl(
                SessionInfo(fin, keyRepo),
                SessionData(keyRepo, sessRepo),
                SessionContext(LocalUniqueKeyTokenRepo(), none()),
                TaskMockFactory.createTaskInfo()
            )
        )
        
        runBlocking {
            sess.addAlias(StringUniqueKey("a"), { Either.catch { } })
            sess.addAlias(StringUniqueKey("a"), { Either.catch { } })
            sess.addAlias(StringUniqueKey("a"), { Either.catch { } })
            
            assertThrows {
                runBlocking {
                    sess.addAlias(StringUniqueKey("a"), { Either.catch { } })
                }
            }
        }
        
    }
    
    
    @Test
    fun testSetDetachable() {
        val mock: BloomFilterFactory = mockk()
        
        every {
            mock.createEmpty()
        } returns (BloomFilterImpl(100, 0.00000001))
        
        val persistFactory = DatabaseAdapterFactoryImpl("jdbc:sqlite::memory:")
        val persister = UniqueKeyPersisterImpl(persistFactory.get())
        
        val keyRepo = spyk(
            CompositeUniqueKeyRepository(
                persister, BloomFilterCache(mock), TemporaryUniqueKeyRepository(), UniqueKeyTokenFactory()
            )
        )
        val fin = FinishObserverImpl()
        val sessRepo = SessionRepositoryImpl(keyRepo, FinishObserverImpl())
        
        val sess: SessionDetachableStartedStateImpl = spyk(
            SessionDetachableStartedStateImpl(
                SessionInfo(fin, keyRepo),
                SessionData(keyRepo, sessRepo),
                SessionContext(LocalUniqueKeyTokenRepo(), none()),
                TaskMockFactory.createTaskInfo()
            )
        )
        
        sess.setDetachable()
        
        verify(exactly = 1) {
            keyRepo.lock_free_notifyMarkedDetachable(any())
        }
    }
    
    @Test
    fun testSetNonDetachable() {
        val mock: BloomFilterFactory = mockk()
        
        every {
            mock.createEmpty()
        } returns (BloomFilterImpl(100, 0.00000001))
        
        val persistFactory = DatabaseAdapterFactoryImpl("jdbc:sqlite::memory:")
        val persister = UniqueKeyPersisterImpl(persistFactory.get())
        
        val keyRepo = spyk(
            CompositeUniqueKeyRepository(
                persister, BloomFilterCache(mock), TemporaryUniqueKeyRepository(), UniqueKeyTokenFactory()
            )
        )
        val fin = FinishObserverImpl()
        val sessRepo = SessionRepositoryImpl(keyRepo, FinishObserverImpl())
        
        val sess: SessionDetachableStartedStateImpl = spyk(
            SessionDetachableStartedStateImpl(
                SessionInfo(fin, keyRepo),
                SessionData(keyRepo, sessRepo),
                SessionContext(LocalUniqueKeyTokenRepo(), none()),
                TaskMockFactory.createTaskInfo()
            )
        )
        
        sess.setNonDetachable()
        
        verify(exactly = 1) {
            keyRepo.lock_free_notifyMarkedNotDetachable(any())
        }
    }
}