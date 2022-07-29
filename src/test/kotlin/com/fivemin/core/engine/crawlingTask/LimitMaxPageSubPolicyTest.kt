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

package com.fivemin.core.engine.crawlingTask


import arrow.core.toOption
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.ElemIterator
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.*
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.util.*

internal class LimitMaxPageSubPolicyTest {
    
    lateinit var limitMaxPageSubPolicy: LimitMaxPageSubPolicy<Request>
    
    @BeforeMethod
    fun before() {
        limitMaxPageSubPolicy = LimitMaxPageSubPolicy(1)
    }
    
    @Test
    fun process() {
        runBlocking {
            limitMaxPageSubPolicy.process(mockk(), mockk(), mockk(), mockk())
        }
        
        val result = runBlocking {
            limitMaxPageSubPolicy.process(mockk(), mockk(), mockk(), mockk())
        }
        
        runBlocking {
            result.await().map {
                fail()
            }
        }
    }
}