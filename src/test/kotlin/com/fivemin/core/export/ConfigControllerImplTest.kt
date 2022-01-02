package com.fivemin.core.export

import org.testng.Assert.*
import org.testng.annotations.Test

class ConfigControllerImplTest {

    @Test
    fun testGetSettings() {
        val configControllerImpl = ConfigControllerImpl("{\"a\" : \"b\"}")

        configControllerImpl.getSettings<String>("a").fold({ fail() }) {
            assertEquals(it, "b")
        }
    }
}
