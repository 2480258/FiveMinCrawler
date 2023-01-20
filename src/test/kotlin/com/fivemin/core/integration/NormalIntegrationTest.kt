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

import arrow.core.Some
import com.fivemin.core.engine.*
import com.fivemin.core.initialize.CrawlerFactory
import com.fivemin.core.initialize.StartTaskOption
import kotlinx.coroutines.runBlocking
import org.testng.annotations.Test
import java.io.File

class NormalIntegrationTest {
    @Test
    fun testNormal() {
        val options = StartTaskOption(
            mainUriTarget = "http://localhost:3000/home",
            paramPath = "TestParameters/jsonIntegrationTest.json"
        )
        
        IntegrationVerify.runAndVerify(
            listOf(
                VerifySet("Output/00.png", 5745),
                VerifySet("Output/01.png", 9004),
                VerifySet("Output/user.json", 41),
                VerifySet("Output/about.json", 41)
            )
        ) {
            CrawlerFactory().get(options).startAndWaitUntilFinish { taskFactory, document, state ->
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
                    task.start(document, state).await()
                }
            }
        }
        
        IntegrationVerify.verifyDirectoryEmpty("Output")
    }
    
    @Test
    fun testPlugin() {
        val options = StartTaskOption(
            mainUriTarget = "http://localhost:3000/home",
            paramPath = "TestParameters/jsonIntegrationTest.json",
            pluginDirectory = Some("./plugins")
        )
        
        IntegrationVerify.runAndVerify(
            listOf(
                VerifySet("Output/00.png", 5745),
                VerifySet("Output/01.png", 9004),
                VerifySet("Output/user.json", 41),
                VerifySet("Output/about.json", 41),
                VerifySet("Output/p1.txt", 94),
                VerifySet("Output/p2.txt", 94),
                VerifySet("Output/p3.txt", 94),
                VerifySet("Output/p4.txt", 94),
                VerifySet("Output/r.txt", 7957)
            )
        ) {
            CrawlerFactory().get(options).startAndWaitUntilFinish { taskFactory, document, state ->
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
                    task.start(document, state).await()
                }
            }
        }
        
        IntegrationVerify.verifyDirectoryEmpty("Output")
    }
    
    @Test
    fun testHeader() {
        val options = StartTaskOption(
            mainUriTarget = "http://localhost:3000/headerReflect",
            paramPath = "TestParameters/jsonIntegrationTest_Header.json"
        )
        
        IntegrationVerify.runAndVerify(listOf(VerifySet("Output/headerReflect.json", 191))) {
            CrawlerFactory().get(options).startAndWaitUntilFinish { taskFactory, document, state ->
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
                    task.start(document, state).await()
                }
            }
            
            val file = File("Output/headerReflect.json")
            assert(file.readText().contains("FiveMinCrawler/1.01111111111111111111111111111111111111111"))
        }
        
        IntegrationVerify.verifyDirectoryEmpty("Output")
    }
    
    //@Test
    fun testRootPath() {
        val options = StartTaskOption(
            mainUriTarget = "http://localhost:3000/headerReflect",
            paramPath = "TestParameters/jsonIntegrationTest_Header.json",
            rootPath = Some("Output11")
        )
        
        IntegrationVerify.runAndVerify(listOf(VerifySet("Output11/Output/headerReflect.json", 276))) {
            CrawlerFactory().get(options).startAndWaitUntilFinish { taskFactory, document, state ->
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
                    task.start(document, state).await()
                }
            }
        }
        
        IntegrationVerify.verifyDirectoryEmpty("Output11/Output")
    }
    
    @Test
    fun testResumeAt() {
        val options = StartTaskOption(
            mainUriTarget = "http://localhost:3000/headerReflect",
            paramPath = "TestParameters/jsonIntegrationTest_Header.json",
            resumeAt = Some("TestParameters/[16]_localhost.d1b")
        )
        
        IntegrationVerify.runAndVerify(listOf()) {
            CrawlerFactory().get(options).startAndWaitUntilFinish { taskFactory, document, state ->
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
                    task.start(document, state).await()
                }
            }
        }
        
        IntegrationVerify.verifyDirectoryEmpty("Output")
    }
    
    
    @Test
    fun testCookie() {
        val options = StartTaskOption(
            mainUriTarget = "http://127.0.0.1:3000/cookieReflect",
            paramPath = "TestParameters/jsonIntegrationTest_Cookie.json"
        )
        
        IntegrationVerify.runAndVerify(listOf(VerifySet("Output/cookieReflect.json", 131))) {
            CrawlerFactory().get(options).startAndWaitUntilFinish { taskFactory, document, state ->
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
                    task.start(document, state).await()
                }
            }
            
            val file = File("Output/cookieReflect.json").readText()
            assert(file.contains("A1111111111111111111113"))
            assert(file.contains("A1111111111111111111112"))
            assert(file.contains("AAAAAAAAAAAAAAAAAAC"))
            assert(file.contains("AAAAAAAAAAAAAAAAAAB"))
        }
        
        IntegrationVerify.verifyDirectoryEmpty("Output")
    }
}