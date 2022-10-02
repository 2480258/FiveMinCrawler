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

package com.fivemin.core.initialize.json

import com.fivemin.core.request.cookie.CookieRepository
import java.net.HttpCookie

@kotlinx.serialization.Serializable
data class JsonCookieImportFormat(
    val cookies: List<JsonCookieDataFormat>
) {
    fun build(): List<HttpCookie> {
        return cookies.map {
            it.build()
        }
    }
}

@kotlinx.serialization.Serializable
data class JsonCookieDataFormat(
    val name: String,
    val value: String,
    val domain: String,
    val path: String? = null,
    val secure: Boolean? = null,
    val httpOnly: Boolean? = null,
    val maxAge: Long? = null
) {
    fun build() : HttpCookie {
        val cookie = HttpCookie(name, value)
        
        cookie.domain = domain
        
        if(path != null) {
            cookie.path = path
        }
        
        if(secure != null) {
            cookie.secure = secure
        }
        
        if(httpOnly != null) {
            cookie.isHttpOnly = httpOnly
        }
        
        if(maxAge != null) {
            cookie.maxAge = maxAge
        }
        
        if(cookie.hasExpired()) {
            throw IllegalStateException("trying to added already expired cookie")
        }
        
        return cookie
    }
}