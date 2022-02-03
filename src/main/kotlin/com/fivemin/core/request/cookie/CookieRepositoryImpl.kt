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
import arrow.core.right
import java.net.HttpCookie
import java.net.URI
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class CookieRepositoryImpl(private val container: CustomCookieJar) : CookieRepository {

    private val lock = ReentrantLock()

    override fun getAllCookies(): Either<Throwable, Iterable<HttpCookie>> {
        lock.withLock {
            return container.cookieStore.cookies.right()
        }
    }

    override fun reset() {
        lock.withLock {
            container.cookieStore.removeAll()
        }
    }

    override fun download(repo: CookieRepository) {
        lock.withLock {
            if (repo == this) {
                return
            }

            var src = repo.getAllCookies()
            var dst = getAllCookies()

            reset()
            src.map { it ->
                it.forEach {
                    container.cookieStore.add(URI(it.domain), it)
                }
            }
        }
    }
}
