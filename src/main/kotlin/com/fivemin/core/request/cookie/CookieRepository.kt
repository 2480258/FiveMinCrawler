package com.fivemin.core.request.cookie

import arrow.core.Either
import java.net.HttpCookie

interface CookieRepository {
    fun getAllCookies(): Either<Throwable, Iterable<HttpCookie>>

    fun reset()

    fun download(repo: CookieRepository)
}
