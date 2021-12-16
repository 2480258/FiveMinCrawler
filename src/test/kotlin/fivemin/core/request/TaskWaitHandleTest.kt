package fivemin.core.request

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.testng.annotations.Test

import org.testng.Assert.*
import org.testng.annotations.BeforeMethod

class TaskWaitHandleTest {

    lateinit var handle: TaskWaitHandle<Int>

    @BeforeMethod
    fun before() {
        handle = TaskWaitHandle()
    }

    @Test
    fun testRun() {
        var num = 0

        runBlocking {
            num = handle.run {
                Thread.sleep(3000)
                handle.registerResult(42)
            }.await()

            assertEquals(num, 42)
        }
    }
}