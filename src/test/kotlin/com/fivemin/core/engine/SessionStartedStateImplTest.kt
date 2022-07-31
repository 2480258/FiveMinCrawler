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
import org.testng.annotations.Test

import org.testng.Assert.*

class SessionStartedStateImplTest {
    
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
                SessionContext(LocalUniqueKeyTokenRepo(), none())
            )
        )
        
        sess.addAlias(StringUniqueKey("a"))
        sess.addAlias(StringUniqueKey("a"))
        sess.addAlias(StringUniqueKey("a"))
    
        assertThrows {
            sess.addAlias(StringUniqueKey("a"))
    
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
        
        val keyRepo = spyk(CompositeUniqueKeyRepository(
            persister, BloomFilterCache(mock), TemporaryUniqueKeyRepository(), UniqueKeyTokenFactory()
        ))
        val fin = FinishObserverImpl()
        val sessRepo = SessionRepositoryImpl(keyRepo, FinishObserverImpl())
        
        val sess: SessionDetachableStartedStateImpl = spyk(
            SessionDetachableStartedStateImpl(
                SessionInfo(fin, keyRepo),
                SessionData(keyRepo, sessRepo),
                SessionContext(LocalUniqueKeyTokenRepo(), none())
            )
        )
        
        sess.setDetachable()
        
        verify (exactly = 1) {
            keyRepo.notifyMarkedDetachable(any())
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
        
        val keyRepo = spyk(CompositeUniqueKeyRepository(
            persister, BloomFilterCache(mock), TemporaryUniqueKeyRepository(), UniqueKeyTokenFactory()
        ))
        val fin = FinishObserverImpl()
        val sessRepo = SessionRepositoryImpl(keyRepo, FinishObserverImpl())
        
        val sess: SessionDetachableStartedStateImpl = spyk(
            SessionDetachableStartedStateImpl(
                SessionInfo(fin, keyRepo),
                SessionData(keyRepo, sessRepo),
                SessionContext(LocalUniqueKeyTokenRepo(), none())
            )
        )
        
        sess.setNonDetachable()
        
        verify (exactly = 1) {
            keyRepo.notifyMarkedNotDetachable(any())
        }
    }
}