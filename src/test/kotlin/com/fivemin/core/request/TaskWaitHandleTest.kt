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

package com.fivemin.core.request

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import org.testng.annotations.Test

import org.testng.Assert.*
import org.testng.annotations.BeforeMethod

class TaskWaitHandleTest {

    lateinit var handle: TaskWaitHandle<Int>

    @BeforeMethod
    fun before() {
        handle = TaskWaitHandle()
    }

    @Test
    fun testRunAsyncCanceled() {
        var num = 0
        val semaphore = Semaphore(1, 1)
        
        runBlocking {
            coroutineScope {
                val job = async {
                    handle.runAsync ({
                        semaphore.acquire()
                        handle.registerResult(42)
                    }, {
                        num = 1
                    }).await()
                }
                
                async {
                    delay(1000)
                    job.cancel()
                }
            }
            
        }
        
        assertEquals(num, 1)
    }
    
    @Test
    fun testRunAsync() {
        var num = 0
        
        runBlocking {
            num = handle.runAsync ({
                delay(3000)
                handle.registerResult(42)
            }, {
            
            }).await()
            
            assertEquals(num, 42)
        }
    }
}