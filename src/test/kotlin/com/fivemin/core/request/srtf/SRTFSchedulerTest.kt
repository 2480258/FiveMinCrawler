package com.fivemin.core.request.srtf

import arrow.core.none
import com.fivemin.core.DocumentMockFactory.Companion.getRequest
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsAttribute
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsRequestReq
import com.fivemin.core.ElemIterator
import com.fivemin.core.StringIterator
import com.fivemin.core.UriIterator
import com.fivemin.core.engine.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.testng.annotations.Test

import org.testng.Assert.*
import org.testng.annotations.BeforeMethod

class SRTFSchedulerTest {

    var sc = SRTFScheduler()
    var prep = SRTFPrepareSubPolicy(sc)
    var final = SRTFFinalizeSubPolicy(sc)
    var export = SRTFExportSubPolicy(sc)

    var uriIt = ElemIterator(UriIterator())
    var strit = ElemIterator(StringIterator())

    @BeforeMethod
    fun before() {
        sc = SRTFScheduler()
        prep = SRTFPrepareSubPolicy(sc)
        final = SRTFFinalizeSubPolicy(sc)
        export = SRTFExportSubPolicy(sc)

        uriIt = ElemIterator(UriIterator())
        strit = ElemIterator(StringIterator())
    }

    private fun mockSessionFunc(mode : WorkingSetMode) : SessionStartedState{
        var mock = mockk<SessionStartedState>()

        val det = when(mode) {
            WorkingSetMode.Enabled -> DetachableState.WANT
            WorkingSetMode.Disabled -> DetachableState.HATE
        }

        every {
            mock.isDetachable
        } returns(det)

        return mock
    }

    private fun addRequestSample(parent : RequestToken?, pageName : String, mode : WorkingSetMode) : FinalizeRequestTransaction<Request> {
        return runBlocking {

            var it = getRequest(uriIt.gen(), RequestType.LINK, parent).upgrade()

            var pp = it.upgradeAsDocument(pageName, mode)
            var ret = prep.process(it, pp, mockk<TaskInfo>(), mockSessionFunc(mode))

            var req = pp.upgradeAsRequestReq().upgrade()
            var fn = pp.upgrade()

            var retfn = final.process(pp, fn, mockk(), mockSessionFunc(mode))

            fn
        }
    }

    private fun addAttributeSample(parent : RequestToken?) : FinalizeRequestTransaction<Request> {
        return runBlocking {
            var it = getRequest(uriIt.gen(), RequestType.ATTRIBUTE, parent).upgrade()
            var pp = it.upgradeAsAttribute()

            var ret = prep.process(it,pp, mockk(), mockSessionFunc(WorkingSetMode.Disabled))
            var fn = pp.upgrade()

            var retfn = final.process(pp, fn, mockk(), mockSessionFunc(WorkingSetMode.Disabled))

            fn
        }
    }

    private fun registerAttribute(parent : RequestToken?) : PrepareTransaction<Request> {
        return runBlocking {
            var it = getRequest(uriIt.gen(), RequestType.ATTRIBUTE, parent).upgrade()
            var pp = it.upgradeAsAttribute()

            var ret = prep.process(it, pp, mockk(), mockSessionFunc(WorkingSetMode.Disabled))
            ret.await().fold({throw it}

            ){}
            pp
        }
    }

    @Test
    fun calcEndPoint() {
        var pg1 = strit.gen()

        var root = addRequestSample(null, "root", WorkingSetMode.Enabled).request.token

        var handle1 = addRequestSample(root, "a", WorkingSetMode.Enabled).request.token
        var handles1 = (0 until 3).map {
            addAttributeSample(handle1)
        }

        var it = getRequest(uriIt.gen(), RequestType.LINK, root).upgrade()

        var pp = it.upgradeAsDocument("a")

        runBlocking {
            var ret = prep.process(it, pp, mockk(), mockSessionFunc(WorkingSetMode.Enabled))
        }
        var req = pp.upgradeAsRequestReq().upgrade()

        assertEquals(sc.getScore(req).toFloat(), 20.0f, 0.1f)
    }

    @Test
    fun calcNonEndPoint() {
        var pg1 = strit.gen()

        var root = addRequestSample(null, "root", WorkingSetMode.Enabled).request.token
        var handle1 = addRequestSample(root, pg1, WorkingSetMode.Enabled).request.token
        var handles1 = (0 until 3).map {
            addAttributeSample(handle1)
        }

        var dohandle = addRequestSample(root, pg1, WorkingSetMode.Enabled).request.token
        var dohandl1 = addAttributeSample(root)

        var pp1 = registerAttribute(dohandle)
        var pp2 = registerAttribute(dohandle)

        var req = pp1.upgradeAsRequestReq(DetachableState.HATE).upgrade()

        assertEquals(sc.getScore(req).toFloat(), 10.0f, 0.1f)
    }

    @Test
    fun calcMemorizationTest() {
        var pg1 = strit.gen()

        var root = addRequestSample(null, "root", WorkingSetMode.Enabled).request.token

        var handle1 = addRequestSample(root, pg1, WorkingSetMode.Enabled).request.token
        var handles1 = (0 until 3).map {
            addAttributeSample(handle1)
        }

        var dohandle = addRequestSample(root, pg1, WorkingSetMode.Enabled).request.token
        var dohandl1 = addAttributeSample(root)

        var pp1 = registerAttribute(dohandle)
        var pp2 = registerAttribute(dohandle)

        var req = pp1.upgradeAsRequestReq(DetachableState.HATE).upgrade()
        var req2 = pp2.upgradeAsRequestReq(DetachableState.HATE).upgrade()

        sc.getScore(req)

        var fn = pp1.upgrade()

        runBlocking {
            final.process(pp1, fn, mockk(), mockSessionFunc(WorkingSetMode.Disabled))
        }

        assertEquals(sc.getScore(req2).toFloat(), 5.0f, 0.1f)
    }

    @Test
    fun calcSingleCycle() {
        runBlocking {
            var it = getRequest(uriIt.gen(), RequestType.LINK).upgrade()

            var pp = it.upgradeAsDocument(strit.gen())

            var ret = prep.process(it, pp, mockk(), mockSessionFunc(WorkingSetMode.Enabled))

            var req = pp.upgradeAsRequestReq().upgrade()

            assertEquals(sc.getScore(req).toFloat(), 0.0f, 0.1f)

            var fn = pp.upgrade()

            var retfn = final.process(pp, fn, mockk(), mockSessionFunc(WorkingSetMode.Enabled))

            var se = fn.upgrade()
            var ex = se.upgrade(null)

            var retex = export.process(se, ex, mockk(), mockSessionFunc(WorkingSetMode.Enabled))

            assert(sc.blockCount == 0 && sc.watchListCount == 1)
        }
    }

    @Test
    fun exportTwice() {

        var it = getRequest(uriIt.gen(), RequestType.LINK).upgrade()

        var pp = it.upgradeAsDocument(strit.gen())

        runBlocking {
            prep.process(it, pp, mockk(), mockSessionFunc(WorkingSetMode.Enabled))
        }

        var req = pp.upgradeAsRequestReq().upgrade()

        assertEquals(sc.getScore(req).toFloat(), 0.0f)

        var fn = pp.upgrade()
        runBlocking {
            final.process(pp, fn, mockk(), mockSessionFunc(WorkingSetMode.Enabled))
        }

        var se = fn.upgrade()
        var ex = se.upgrade(null)

        runBlocking {
            var retex = export.process(se, ex, mockk(), mockSessionFunc(WorkingSetMode.Enabled))
            export.process(se, ex, mockk(), mockSessionFunc(WorkingSetMode.Enabled))
        }

        assert(sc.blockCount == 0 && sc.watchListCount == 1)
    }

    @Test
    fun finalizeTwice() {
        var it = getRequest(uriIt.gen(), RequestType.LINK).upgrade()

        var pp = it.upgradeAsDocument(strit.gen())

        runBlocking {
            prep.process(it, pp, mockk(), mockSessionFunc(WorkingSetMode.Enabled))
        }

        var req = pp.upgradeAsRequestReq().upgrade()

        assertEquals(sc.getScore(req).toFloat(), 0.0f)

        var fn = pp.upgrade()
        runBlocking {
            final.process(pp, fn, mockk(), mockSessionFunc(WorkingSetMode.Enabled))
            final.process(pp, fn, mockk(), mockSessionFunc(WorkingSetMode.Enabled))
        }

        var se = fn.upgrade()
        var ex = se.upgrade(null)

        runBlocking {
            var retex = export.process(se, ex, mockk(), mockSessionFunc(WorkingSetMode.Enabled))
        }

        assert(sc.blockCount == 0 && sc.watchListCount == 1)
    }


    @Test
    fun prepareTwice() {
        var it = getRequest(uriIt.gen(), RequestType.LINK).upgrade()

        var pp = it.upgradeAsDocument(strit.gen())

        runBlocking {
            prep.process(it, pp, mockk(), mockSessionFunc(WorkingSetMode.Enabled))
            prep.process(it, pp, mockk(), mockSessionFunc(WorkingSetMode.Enabled))
        }

        var req = pp.upgradeAsRequestReq().upgrade()

        assertEquals(sc.getScore(req).toFloat(), 0.0f, 0.1f)

        var fn = pp.upgrade()

        runBlocking {
            var retfn = final.process(pp, fn, mockk(), mockSessionFunc(WorkingSetMode.Enabled))

            var se = fn.upgrade()
            var ex = se.upgrade(null)

            var retex = export.process(se, ex, mockk(), mockSessionFunc(WorkingSetMode.Enabled))
        }

        kotlin.test.assertTrue {
            sc.blockCount == 0 && sc.watchListCount == 1
        }
    }

}