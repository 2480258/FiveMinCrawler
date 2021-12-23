package fivemin.core.request.cookie

import arrow.core.Either
import fivemin.core.engine.PerformedRequesterInfo

interface CookieRepositoryReferenceSolver {
    fun getReference(info : PerformedRequesterInfo) : Either<Throwable, CookieRepository>
}