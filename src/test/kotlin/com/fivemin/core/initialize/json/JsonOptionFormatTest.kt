package com.fivemin.core.initialize.json

import com.fivemin.core.engine.DirectIO
import com.fivemin.core.engine.DirectoryIOToken
import io.mockk.every
import io.mockk.mockk
import org.testng.Assert.*
import org.testng.annotations.Test
import java.io.File

class JsonOptionFormatTest {
    @Test
    fun test() {

        val mock = mockk<DirectIO>()

        every {
            mock.getToken(any())
        } returns(DirectoryIOToken("testResult"))

        val format = JsonParserOptionFactory(File("jsonReadUnitTest.json").readText(), mockk(), mock)
    }
}
