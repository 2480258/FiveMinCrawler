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

package com.fivemin.core.engine.session.database

import org.testng.annotations.Test


class DatabaseAdapterImplTest {
    
    lateinit var adapterImpl: DatabaseAdapterImpl
    
    @Test
    fun insertIfNoneInputTest() {
        adapterImpl = DatabaseAdapterImpl("jdbc:sqlite::memory:")
        
        assert(adapterImpl.insertKeyIfNone("test"))
    }
    
    @Test
    fun insertIfNoneDuplicatedTest() {
        adapterImpl = DatabaseAdapterImpl("jdbc:sqlite::memory:")
        
        adapterImpl.insertKeyIfNone("test")
        assert(!adapterImpl.insertKeyIfNone("test"))
    }
    
    @Test
    fun containsWhenNotContainedTest() {
        adapterImpl = DatabaseAdapterImpl("jdbc:sqlite::memory:")
        
        assert(!adapterImpl.contains("test"))
    }
    
    @Test
    fun containsWhenContainsTest() {
        adapterImpl = DatabaseAdapterImpl("jdbc:sqlite::memory:")
        
        adapterImpl.insertKeyIfNone("test")
        assert(adapterImpl.contains("test"))
    }
}