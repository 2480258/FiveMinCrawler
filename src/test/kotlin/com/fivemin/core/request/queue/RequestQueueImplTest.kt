package com.fivemin.core.request.queue

import com.fivemin.core.DocumentMockFactory.Companion.getRequest
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsRequestReq
import com.fivemin.core.ElemIterator
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.RequestType
import com.fivemin.core.request.DequeueDecision
import com.fivemin.core.request.DequeueDecisionFactory
import com.fivemin.core.request.EnqueueRequestInfo
import io.mockk.every
import io.mockk.mockk
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

import java.sql.Timestamp
import java.time.LocalDate
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
    fun dequeueDeniedTest() {
        val deq = RequestQueueImpl(d!!, 1)
        val ev = CountDownLatch(1)

        val q = mockk<DequeueDecisionFactory>()
        every {
            q.get()
        } returns (DequeueDecision.DENY)

        var req =
            getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("").upgradeAsRequestReq().upgrade(q)


        deq.enqueue(req, EnqueueRequestInfo {
            it.fold({
                ev.countDown()
            }) {
                fail()
            }
        })

        ev.await()

    }

    @Test
    fun dequeueSuccTest() {
        val deq = RequestQueueImpl(d!!, 1)
        val ev = CountDownLatch(1)

        val q = mockk<DequeueDecisionFactory>()
        every {
            q.get()
        } returns (DequeueDecision.ALLOW)

        var req =
            getRequest(uriIt.gen(), RequestType.LINK).upgrade().upgradeAsDocument("").upgradeAsRequestReq().upgrade(q)


        deq.enqueue(req, EnqueueRequestInfo {
            it.fold({
                fail()
            }) {
                ev.countDown()
            }
        })

        ev.await()
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