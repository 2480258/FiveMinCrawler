package fivemin.core.request.cookie

import fivemin.core.engine.PerformedRequesterInfo

interface CookieResolveTargetFactory : CookieRepositoryReferenceSolver {
    fun create(info : PerformedRequesterInfo, cookieRepo : CookieRepository) : CookieResolveTarget
}