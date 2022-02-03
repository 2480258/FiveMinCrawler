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

package com.fivemin.core.request

import arrow.core.Option
import arrow.core.Some
import arrow.core.none

enum class AcceptHeaderType {
    DEFAULT, IMAGE, VIDEO, AUDIO, SCRIPTS, CSS
}

interface AcceptHeaderPolicy {
    fun getHeader(headerType: AcceptHeaderType): Option<String>
}

class FirefoxAcceptHeaderPolicyImpl : AcceptHeaderPolicy {
    override fun getHeader(headerType: AcceptHeaderType): Option<String> {
        return when (headerType) {
            AcceptHeaderType.DEFAULT -> Some("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.5")
            AcceptHeaderType.IMAGE -> Some("image/webp,*/*")
            AcceptHeaderType.CSS -> Some("text/css,*/*;q=0.1")
            AcceptHeaderType.SCRIPTS -> Some("*/*")
            AcceptHeaderType.VIDEO -> none()
            AcceptHeaderType.AUDIO -> Some("audio/webm,audio/ogg,audio/wav,audio/*;q=0.9,application/ogg;q=0.7,video/*;q=0.6,*/*;q=0.5")
            else -> none()
        }
    }
}

data class RequestHeaderProfile(
    val acceptEncoding: Option<String> = Some("gzip, deflate, br"),
    val acceptLanguage: Option<String> = Some("en-US,en;q=0.5"),
    val connection: Option<String> = Some("Keep-Alive"),
    val te: Option<String> = Some("trailers"),
    val userAgent: Option<String> = none()
)
