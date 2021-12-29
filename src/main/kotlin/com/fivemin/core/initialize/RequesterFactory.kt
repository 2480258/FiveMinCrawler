package com.fivemin.core.initialize

import com.fivemin.core.engine.DirectIO
import com.fivemin.core.engine.PerformedRequesterInfo
import com.fivemin.core.engine.ResponseData
import com.fivemin.core.request.RequesterCore
import com.fivemin.core.request.cookie.CookieResolveTargetFactory

interface RequesterFactory {
    val key : String
    fun build(info: PerformedRequesterInfo, io : DirectIO, factory : CookieResolveTargetFactory) : RequesterCore<ResponseData>
}