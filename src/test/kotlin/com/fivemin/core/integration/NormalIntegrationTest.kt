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

class NormalIntegrationTest {
    @Test
    fun testNormal() {
        val options = StartTaskOption(
            mainUriTarget = "http://localhost:3000/home",
            paramPath = "jsonIntegrationTest.json",
            rootPath = Some("TrashBin")
        )
        
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
}