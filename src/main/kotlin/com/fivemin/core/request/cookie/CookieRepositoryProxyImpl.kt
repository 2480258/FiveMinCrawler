package com.fivemin.core.request.cookie

import arrow.core.Either
import arrow.core.flatten
import com.fivemin.core.engine.PerformedRequesterInfo
import java.net.HttpCookie

class CookieRepositoryProxyImpl(private val solver: CookieRepositoryReferenceSolver, private val dest: PerformedRequesterInfo) :
    CookieRepository {
    var cache: Either<Throwable, CookieRepository>? = null

    override fun getAllCookies(): Either<Throwable, Iterable<HttpCookie>> {
        return getRepo().map {
            it.getAllCookies()
        }.flatten()
    }

    private fun getRepo(): Either<Throwable, CookieRepository> {
        if (cache == null) {
            cache = solver.getReference(dest)
        }

        return cache!!
    }

    override fun download(repo: CookieRepository) {
        getRepo().map {
            it.download(repo)
        }
    }

    override fun reset() {
        getRepo().map {
            it.reset()
        }
    }
}
