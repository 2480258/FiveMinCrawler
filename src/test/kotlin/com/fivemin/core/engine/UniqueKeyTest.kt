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

import com.fivemin.core.ElemIterator
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.session.UriUniqueKey
import com.fivemin.core.engine.transaction.StringUniqueKey
import org.testng.annotations.Test

import org.testng.Assert.*
import org.testng.annotations.BeforeMethod

class UniqueKeyTest {
    
    var uriIt = ElemIterator(UriIterator())
    
    @BeforeMethod
    fun before() {
        uriIt = ElemIterator(UriIterator())
    }
    
    @Test
    fun testUriStringKey() {
        val uri = StringUniqueKey(uriIt.gen().toString())
        val uri2 = StringUniqueKey(uriIt[0].toString())
        
        assert(uri == uri2)
    }
    
    
    @Test
    fun testUriUniqueKey() {
        val uri = UriUniqueKey(uriIt.gen())
        val uri2 = UriUniqueKey(uriIt[0]!!)
        
        assert(uri == uri2)
    }
}