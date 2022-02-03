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

import arrow.core.*
import com.fivemin.core.engine.PerformedRequesterInfo

class CookieControllerImpl(val gradiant: List<CookieSyncGradiant>) : CookieResolveTargetFactory {

    val cookieDic: MutableMap<PerformedRequesterInfo, CookieRepository> = mutableMapOf()

    override fun create(info: PerformedRequesterInfo, cookieRepo: CookieRepository): CookieResolveTarget {
        cookieDic[info] = cookieRepo

        return CookieResolveTargetImpl(
            cookieRepo,
            OutwardPolicy(
                gradiant.filter {
                    it.source == info
                }.flatMap {
                    it.destination.map {
                        CookieRepositoryProxyImpl(this, it)
                    }
                }
            )
        )
    }

    override fun getReference(info: PerformedRequesterInfo): Either<Throwable, CookieRepository> {
        if (cookieDic.containsKey(info)) {
            return cookieDic[info]!!.right()
        }

        return NotRecognizedCookieSyncExcepion(info.toString()).left()
    }
}
