/*
 *
 *     FiveMinCrawler
 *     Copyright (C) 2022  2480258
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.fivemin.core.request.cookie
import okhttp3.*
import java.net.*

class CustomCookieJar constructor(cookies: List<HttpCookie>? = null) : CookieJar {
    private val cookiejar: CookieJar
    private val manager: CookieManager = CookieManager()

    val cookieStore: CookieStore
        get() {
            return manager.cookieStore
        }

    init {
        manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        cookiejar = JavaNetCookieJar(manager)
    
        cookies?.forEach {
            manager.cookieStore.add(URI(it.domain), it)
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookiejar.loadForRequest(url)
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        return cookiejar.saveFromResponse(url, cookies)
    }
}
