package core.request.cookie

import arrow.core.Validated
import core.engine.PerformedRequesterInfo

interface CookieRepositoryReferenceSolver {
    fun getReference(info : PerformedRequesterInfo) : Validated<Throwable, CookieRepository>
}