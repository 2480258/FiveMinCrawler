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

import org.testng.Assert.fail
import java.io.File

data class VerifySet(val name: String, val size: Long)

class IntegrationVerify {
    companion object {
        fun verifyDirectoryEmpty(directory: String) {
            val file = File(directory)
            
            if (file.isDirectory and file.listFiles().any()) {
                fail()
            }
        }
        
        fun runAndVerify(verifySet: List<VerifySet>, func: () -> Unit) {
            val files = verifySet.map {
                Pair(File(it.name), it.size)
            }
            try {
                for (file in files) {
                    if (file.first.isDirectory) {
                        throw IllegalStateException("VerifySet is a directory: " + file.first.name)
                    }
                    
                    if (file.first.exists()) {
                        file.first.delete()
                        println("VerifySet deleted before run: " + file.first.name)
                    }
                }
                
                func()
                
                for (file in files) {
                    if (file.first.isDirectory) {
                        throw IllegalStateException("VerifySet is a directory: " + file.first.name)
                    }
                    
                    if (!file.first.exists()) {
                        fail("VerifySet failed to verify: file didn't exists: " + file.first.name)
                    }
                    
                    if (file.first.length() != file.second) {
                        fail("VerifySet failed to verify: file didn't matches: " + file.first.name + ", current: " + file.first.length() + ", expected: " + file.second)
                    }
                }
            } finally {
                for (file in files) {
                    if (file.first.isDirectory) {
                        throw IllegalStateException("VerifySet is a directory: " + file.first.name)
                    }
                    
                    if (file.first.exists()) {
                        file.first.delete()
                        println("VerifySet deleted after run: " + file.first.name)
                    }
                }
            }
        }
    }
}