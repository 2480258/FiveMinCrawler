package core.initialize

import core.engine.DirectIO
import core.engine.PerformedRequesterInfo
import core.engine.ResponseData
import core.request.RequesterCore
import core.request.cookie.CookieResolveTargetFactory

interface RequesterFactory {
    val key : String
    fun build(info: PerformedRequesterInfo, io : DirectIO, factory : CookieResolveTargetFactory) : RequesterCore<ResponseData>
}