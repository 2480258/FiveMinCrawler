package com.fivemin.core.request.cookie
import okhttp3.*
import java.io.IOException
import java.util.Collections
import okhttp3.internal.cookieToString
import okhttp3.internal.delimiterOffset
import okhttp3.internal.platform.Platform
import okhttp3.internal.platform.Platform.Companion.WARN
import okhttp3.internal.trimSubstring
import java.net.*

class CustomCookieJar : CookieJar {
    private val cookiejar : CookieJar
    private val manager : CookieManager

    val cookieStore : CookieStore
    get() {
        return manager.cookieStore
    }

    init{
        manager = CookieManager()
        manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        cookiejar = JavaNetCookieJar(manager)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookiejar.loadForRequest(url)
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        return cookiejar.saveFromResponse(url, cookies)
    }
}