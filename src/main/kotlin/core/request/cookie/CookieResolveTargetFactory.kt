package core.request.cookie

import core.engine.PerformedRequesterInfo

interface CookieResolveTargetFactory : CookieRepositoryReferenceSolver {
    fun create(info : PerformedRequesterInfo, cookieRepo : CookieRepository) : CookieResolveTarget
}