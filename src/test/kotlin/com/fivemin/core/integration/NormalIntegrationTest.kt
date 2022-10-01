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

package com.fivemin.core.integration

import com.fivemin.core.engine.*
import com.fivemin.core.initialize.CrawlerFactory
import com.fivemin.core.initialize.StartTaskOption
import kotlinx.coroutines.runBlocking
import org.testng.annotations.Test

class NormalIntegrationTest {
    @Test
    fun testNormal() {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")
        
        val options = StartTaskOption(
            mainUriTarget = "http://localhost:3000/home",
            paramPath = "TestParameters/jsonIntegrationTest.json"
        )
        
        IntegrationVerify.runAndVerify(listOf(VerifySet("Output/00.png", 5745), VerifySet("Output/01.png", 9004), VerifySet("Output/user.json", 41), VerifySet("Output/about.json", 41))) {
            CrawlerFactory().get(options).startAndWaitUntilFinish { taskFactory, document, info, state ->
                val task = taskFactory.getFactory()
                    .get4<
                            InitialTransaction<Request>,
                            PrepareTransaction<Request>,
                            FinalizeRequestTransaction<Request>,
                            SerializeTransaction<Request>,
                            ExportTransaction<Request>>(
                        DocumentType.DEFAULT
                    )
        
                runBlocking {
                    task.start(document, info, state)
                }
            }
        }
        
        IntegrationVerify.verifyDirectoryEmpty("Output")
    }
    
    @Test
    fun testCascading() {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")
        
        val options = StartTaskOption(
            mainUriTarget = "http://localhost:3000/home",
            paramPath = "TestParameters/jsonIntegrationTest_WSDisabled.json"
        )
        
        IntegrationVerify.runAndVerify(listOf()) {
            CrawlerFactory().get(options).startAndWaitUntilFinish { taskFactory, document, info, state ->
                val task = taskFactory.getFactory()
                    .get4<
                            InitialTransaction<Request>,
                            PrepareTransaction<Request>,
                            FinalizeRequestTransaction<Request>,
                            SerializeTransaction<Request>,
                            ExportTransaction<Request>>(
                        DocumentType.DEFAULT
                    )
                
                runBlocking {
                    task.start(document, info, state)
                }
            }
        }
        
        IntegrationVerify.verifyDirectoryEmpty("Output")
    }
    
}