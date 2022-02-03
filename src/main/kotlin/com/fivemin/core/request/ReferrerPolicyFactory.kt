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
import java.net.URI

enum class ReferrerPolicy {
    NO_REFERRER,
    UNSAFE_URL,
    ORIGIN,
    STRICT_ORIGIN,
    NO_REFERRER_WHEN_DOWNGRADE,
    SAME_ORIGIN,
    ORIGIN_WHEN_CROSS_ORIGIN,
    STRICT_ORIGIN_WHEN_CROSS_ORIGIN
}

class ReferrerPolicyFactory {
    private val NO_REFERRER = "no-referrer"
    private val NO_REFERRER_WHEN_DOWNGRADE = "no-referrer-when-downgrade"
    private val ORIGIN = "origin"
    private val ORIGIN_WHEN_CROSS_ORIGIN = "origin-when-cross-origin"
    private val SAME_ORIGIN = "same-origin"
    private val STRICT_ORIGIN = "strict-origin"
    private val STRICT_ORIGIN_WHEN_CROSS_ORIGIN = "strict-origin-when-cross-origin"
    private val UNSAFE_URL = "unsafe-url"

    private val DEFAULT_REFERRER =
        ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN // Default value for contemporary browsers

    private fun parseReferrerPolicy(referrer: Option<String>): ReferrerPolicy {
        return referrer.fold({ DEFAULT_REFERRER }, {
            return when (it) {
                NO_REFERRER -> ReferrerPolicy.NO_REFERRER
                NO_REFERRER_WHEN_DOWNGRADE -> ReferrerPolicy.NO_REFERRER_WHEN_DOWNGRADE //
                ORIGIN -> ReferrerPolicy.ORIGIN
                ORIGIN_WHEN_CROSS_ORIGIN -> ReferrerPolicy.ORIGIN_WHEN_CROSS_ORIGIN
                SAME_ORIGIN -> ReferrerPolicy.SAME_ORIGIN
                STRICT_ORIGIN -> ReferrerPolicy.STRICT_ORIGIN
                STRICT_ORIGIN_WHEN_CROSS_ORIGIN -> ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN
                UNSAFE_URL -> ReferrerPolicy.UNSAFE_URL
                else -> DEFAULT_REFERRER
            }
        })
    }

    private fun isSameDomain(src: URI, dest: URI): Boolean {
        return src.host.lowercase() == dest.host.lowercase()
    }

    private fun isHttps(src: URI): Boolean {
        return src.scheme.lowercase() == "https"
    }

    private fun onlyDomain(src: URI): URI {
        return URI(src.scheme + "://" + src.authority + "/")
    }

    fun extractReferrer(src: URI, dest: URI, referrerPolicy: Option<String>): Option<URI> {
        val ret = parseReferrerPolicy(referrerPolicy)

        return when (ret) {
            ReferrerPolicy.NO_REFERRER -> none()
            ReferrerPolicy.UNSAFE_URL -> Some(src)
            ReferrerPolicy.ORIGIN -> Some(onlyDomain(src))
            ReferrerPolicy.STRICT_ORIGIN -> if (isHttps(src)) {
                Some(onlyDomain(src))
            } else {
                none()
            }
            ReferrerPolicy.NO_REFERRER_WHEN_DOWNGRADE -> if (isHttps(dest)) {
                Some(src)
            } else {
                none()
            }
            ReferrerPolicy.SAME_ORIGIN -> if (isSameDomain(src, dest)) {
                Some(src)
            } else {
                none()
            }

            ReferrerPolicy.ORIGIN_WHEN_CROSS_ORIGIN -> if (isSameDomain(src, dest)) {
                Some(src)
            } else {
                Some(onlyDomain(src))
            }
            ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN -> if (!isHttps(src)) {
                none()
            } else if (isSameDomain(src, dest)) {
                Some(src)
            } else {
                none()
            }
        }
    }
}
