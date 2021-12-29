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