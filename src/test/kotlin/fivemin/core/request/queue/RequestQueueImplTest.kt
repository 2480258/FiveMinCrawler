package fivemin.core.request.queue

import fivemin.core.DocumentMockFactory.Companion.getRequest
import fivemin.core.DocumentMockFactory.Companion.upgrade
import fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import fivemin.core.DocumentMockFactory.Companion.upgradeAsRequestReq
import fivemin.core.ElemIterator
import fivemin.core.UriIterator
import fivemin.core.engine.RequestType
import fivemin.core.request.DequeueDecision
import fivemin.core.request.DequeueDecisionFactory
import fivemin.core.request.EnqueueRequestInfo
import io.mockk.every
import io.mockk.mockk
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import java.sql.Timestamp;
import java.time.LocalDate;
import org.testng.Assert.*
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch

class RequestQueueImplTest {

    var uriIt = ElemIterator(UriIterator())
    var d: DequeueOptimizationPolicy? = null

    @BeforeMethod
    fun setUp() {
        d = mockk<DequeueOptimizationPolicy>()

        every {
            d!!.getScore(any())
        } returns (0.0)
    }

    @AfterMethod
    fun tearDown() {
    }

    @Test
    fun dequeueDelayTest() {
        val deq = RequestQueueImpl(d!!, 1)
        val ev = CountDownLatch(1)
        val q = mockk<DequeueDecisionFactory>()

        every {
            q.get()
        } returns (DequeueDecision.DELAY)


        var req =
            getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("").upgradeAsRequestReq().upgrade(q)

        val time = Timestamp.valueOf(LocalDateTime.now())

        deq.enqueue(req, EnqueueRequestInfo {
            it.fold({
                ev.countDown()
            }) {
                fail()
            }
        })

        ev.await()
        assertEquals((Timestamp.valueOf(LocalDateTime.now()).time - time.time) > 10000, true )
    }
}