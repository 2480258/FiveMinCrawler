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

import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import org.testng.Assert.*

class MemoryWriterImplTest {

    val w = MemoryWriterImpl()

    @BeforeMethod
    fun setUp() {
    }

    @AfterMethod
    fun tearDown() {
    }

    @Test
    fun migrateTest() {

        var sw = false

        var arr = ByteArray(9000) {
            (it % Byte.MAX_VALUE - 1).toByte()
        }

        w.write(arr, 0, 9000)

        val d = DiskWriterImpl(RequestToken.create(), DirectoryIOToken(System.getProperty("user.dir")))

        w.migrateMeToAndDisposeThis(d)
        d.flushAndExportAndDispose().openStreamAsByteAndDispose {
            sw = true
            assertEquals(it.available(), 9000)
        }

        assertEquals(sw, true)
    }

    @Test
    fun testWrite() {
        var arr = ByteArray(64) {
            it.toByte()
        }

        var sw = false

        w.write(arr, 0, 64)
        w.flushAndExportAndDispose().openStreamAsByteAndDispose {
            sw = true
            assertEquals(it.available(), 64)
        }

        assertEquals(sw, true)
    }
}