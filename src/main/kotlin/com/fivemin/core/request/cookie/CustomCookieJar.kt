package com.fivemin.core.request.cookie
import okhttp3.*
import java.net.*

class CustomCookieJar : CookieJar {
    private val cookiejar: CookieJar
    private val manager: CookieManager = CookieManager()

    val cookieStore: CookieStore
        get() {
            return manager.cookieStore
        }

    init {
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
