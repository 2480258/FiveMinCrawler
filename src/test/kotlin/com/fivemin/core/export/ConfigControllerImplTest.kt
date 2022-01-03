package com.fivemin.core.export

import org.testng.Assert.*
import org.testng.annotations.Test

class ConfigControllerImplTest {
    @Test
    fun testGetSettingsString() {
        val configControllerImpl = ConfigControllerImpl("{\"a\" : \"b\"}")

        configControllerImpl.getSettings("a").fold({ fail() }) {
            assertEquals(it, "b")
        }
    }
}
