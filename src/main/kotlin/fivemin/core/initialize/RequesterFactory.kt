package fivemin.core.initialize

import fivemin.core.engine.DirectIO
import fivemin.core.engine.PerformedRequesterInfo
import fivemin.core.engine.ResponseData
import fivemin.core.request.RequesterCore
import fivemin.core.request.cookie.CookieResolveTargetFactory

interface RequesterFactory {
    val key : String
    fun build(info: PerformedRequesterInfo, io : DirectIO, factory : CookieResolveTargetFactory) : RequesterCore<ResponseData>
}