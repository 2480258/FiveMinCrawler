package fivemin.core.request.cookie

import arrow.core.Validated
import fivemin.core.engine.PerformedRequesterInfo

interface CookieRepositoryReferenceSolver {
    fun getReference(info : PerformedRequesterInfo) : Validated<Throwable, CookieRepository>
}