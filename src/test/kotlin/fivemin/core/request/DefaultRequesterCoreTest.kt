package fivemin.core.request

import fivemin.core.engine.PerformedRequesterInfo
import fivemin.core.engine.RequesterEngineInfo
import fivemin.core.engine.RequesterSlotInfo
import fivemin.core.request.cookie.CookieResolveTarget
import fivemin.core.request.cookie.CookieResolveTargetFactory
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test


class DefaultRequesterCoreTest {

    @MockK
    var performedRequesterInfo: PerformedRequesterInfo = PerformedRequesterInfo(RequesterEngineInfo("a"), RequesterSlotInfo(0))

    @MockK
    lateinit var cookieResolveTargetFactory: CookieResolveTargetFactory

    @SpyK
    lateinit var target : CookieResolveTarget

    @MockK
    lateinit var adapter: RequesterAdapter

    init {
        MockKAnnotations.init(this, relaxUnitFun = true)

        every {
            cookieResolveTargetFactory.create(any(), any())
        } returns target
    }

    @Test
    fun requestTest() {
        var core = DefaultRequesterCore(
            RequesterExtraImpl(), performedRequesterInfo, HttpRequesterConfig(
                RequesterConfig(cookieResolveTargetFactory), RequestHeaderProfile()
            ), adapter
        )
    }

}