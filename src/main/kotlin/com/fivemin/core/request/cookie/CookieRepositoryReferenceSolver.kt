package com.fivemin.core.request.cookie

import arrow.core.Either
import com.fivemin.core.engine.PerformedRequesterInfo

interface CookieRepositoryReferenceSolver {
    fun getReference(info : PerformedRequesterInfo) : Either<Throwable, CookieRepository>
}