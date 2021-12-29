package com.fivemin.core.request.cookie

import com.fivemin.core.engine.PerformedRequesterInfo

interface CookieResolveTargetFactory : CookieRepositoryReferenceSolver {
    fun create(info : PerformedRequesterInfo, cookieRepo : CookieRepository) : CookieResolveTarget
}