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

import com.fivemin.core.engine.FinishObserver
import io.mockk.mockk
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import kotlin.concurrent.thread

class FinishObserverImplTest {
    
    @BeforeMethod
    fun before() {
        bf = FinishObserverImpl()
    }
    
    lateinit var bf: FinishObserver
    
    @Test
    fun testOnStart() {
        bf.onStart()
        bf.onFinish(mockk())
        
        bf.waitFinish()
    }
    
    @Test
    fun testOnStartThrowsOnNegative() {
        Assert.assertThrows {
            bf.onFinish(mockk())
        }
    }
    
    @Test
    fun testWaitFinish() {
        val t1 = thread {
            bf.waitFinish()
        }
        
        val t2 = thread {
            bf.onStart()
            bf.onFinish(mockk())
        }
        
        t1.join()
    }
}