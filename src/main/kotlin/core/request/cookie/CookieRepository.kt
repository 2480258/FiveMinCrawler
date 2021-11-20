package core.request.cookie

import arrow.core.Validated
import java.net.HttpCookie

interface CookieRepository {
    fun getAllCookies() : Validated<Throwable, Iterable<HttpCookie>>

    fun reset()

    fun download(repo : CookieRepository)
}