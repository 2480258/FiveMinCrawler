package com.fivemin.core.engine.crawlingTask


import arrow.core.toOption
import com.fivemin.core.DocumentMockFactory
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.ElemIterator
import com.fivemin.core.StubMockFactory.Companion.mockInfo
import com.fivemin.core.StubMockFactory.Companion.mockState
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.*
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.util.*

internal class LimitMaxPageSubPolicyTest {
    
    lateinit var limitMaxPageSubPolicy: LimitMaxPageSubPolicy<Request>
    
    @BeforeMethod
    fun before() {
        limitMaxPageSubPolicy = LimitMaxPageSubPolicy(1)
    }
    
    @Test
    fun process() {
        runBlocking {
            limitMaxPageSubPolicy.process(mockk(), mockk(), mockk(), mockk())
        }
        
        val result = runBlocking {
            limitMaxPageSubPolicy.process(mockk(), mockk(), mockk(), mockk())
        }
        
        runBlocking {
            result.await().map {
                fail()
            }
        }
    }
}