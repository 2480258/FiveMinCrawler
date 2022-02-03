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

import arrow.core.Either
import arrow.core.flatten
import com.fivemin.core.engine.PerformedRequesterInfo
import java.net.HttpCookie

class CookieRepositoryProxyImpl(private val solver: CookieRepositoryReferenceSolver, private val dest: PerformedRequesterInfo) :
    CookieRepository {
    var cache: Either<Throwable, CookieRepository>? = null

    override fun getAllCookies(): Either<Throwable, Iterable<HttpCookie>> {
        return getRepo().map {
            it.getAllCookies()
        }.flatten()
    }

    private fun getRepo(): Either<Throwable, CookieRepository> {
        if (cache == null) {
            cache = solver.getReference(dest)
        }

        return cache!!
    }

    override fun download(repo: CookieRepository) {
        getRepo().map {
            it.download(repo)
        }
    }

    override fun reset() {
        getRepo().map {
            it.reset()
        }
    }
}
