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

import arrow.core.none
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import org.testng.Assert.*

class TranslatableFilterTest {

    val w = TranslatableFilter(none(), RequestToken.create(), DirectoryIOToken(".\\"))

    @BeforeMethod
    fun setUp() {
    }

    @AfterMethod
    fun tearDown() {
    }

    @Test
    fun testWrite() {
        var array = ByteArray(10000) {
            (it % (Byte.MAX_VALUE - 1)).toByte()
        }

        var sw = false

        w.write(array, 0, array.size)
        w.flushAndExportAndDispose().openStreamAsByteAndDispose {
            sw = true
            assertEquals(it.available(), 10000)
        }

        assertEquals(sw, true)
    }
}