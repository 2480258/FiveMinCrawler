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