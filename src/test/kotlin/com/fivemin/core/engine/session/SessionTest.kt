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
import com.fivemin.core.engine.LocalUniqueKeyTokenRepo
import com.fivemin.core.engine.SessionDetachableStartedStateImpl
import com.fivemin.core.engine.SessionInfo
import org.testng.Assert.assertThrows
import org.testng.annotations.BeforeMethod
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test

class SessionTest {

}

class LocalUniqueKeyRepoTest {
    lateinit var repo: LocalUniqueKeyTokenRepo
    
    val tokenFac = UniqueKeyTokenFactory()
    val it = ElemIterator(UniqueKeyIterator())
    
    @BeforeTest
    fun before() {
        repo = LocalUniqueKeyTokenRepo()
    }
    
    @Test
    fun addTest() {
        repo.add(it.gen(), tokenFac.create(it[0]!!))
        
        assertThrows {
            repo.add(it[0]!!, tokenFac.create(it[0]!!))
        }
    }
    
    @Test
    fun updateTest() {
        repo.add(it.gen(), tokenFac.create(it[0]!!))
        repo.update(it[0]!!)
        repo.update(it[0]!!)
    
        assertThrows {
            repo.update(it[0]!!)
        }
    }
    
    @Test
    fun updateNonKeyTest() {
        assertThrows {
            repo.update(it.gen())
        }
    }
    
    @Test
    fun containsTest() {
        assert(!repo.contains(it.gen()))
    }
    
    
    @Test
    fun containsExistsTest() {
        repo.add(it.gen(), tokenFac.create(it[0]!!))
        assert(repo.contains(it[0]!!))
    }
}