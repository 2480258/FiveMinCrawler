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

package com.fivemin.core.engine

import arrow.core.Option
import arrow.core.flatten
import arrow.core.none
import com.fivemin.core.request.*
import java.net.URI

/**
 * Request parameters can be used for one-time.
 */
class PerRequestHeaderProfile(
    val requestHeaderProfile: Option<RequestHeaderProfile>,
    referrerPolicy: Option<String>,
    src: Option<URI>,
    dest: URI
) {
    private val referrerPolicyFactory: ReferrerPolicyFactory

    val referrer: Option<URI>

    init {
        referrerPolicyFactory = ReferrerPolicyFactory()

        referrer = src.map {
            referrerPolicyFactory.extractReferrer(it, dest, referrerPolicy)
        }.flatten()
    }
}
