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

import com.fivemin.core.ElemIterator
import com.fivemin.core.UriIterator
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.net.HttpCookie

class CookieRepositoryImplTest {

    lateinit var repo: CookieRepository
    lateinit var jar: CustomCookieJar
    var uriIt = ElemIterator(UriIterator())

    @BeforeMethod
    fun beforeMethod() {
        jar = CustomCookieJar()
        repo = CookieRepositoryImpl(jar)
        uriIt = ElemIterator(UriIterator())
    }

    @Test
    fun testGetAllCookies() {
        jar.cookieStore.add(uriIt.gen(), HttpCookie("a", "b"))
        repo.getAllCookies_Interlocked().fold({ fail() }) {
            assertEquals(it.count(), 1)
        }
    }

    @Test
    fun testReset() {
        jar.cookieStore.add(uriIt.gen(), HttpCookie("a", "b"))
        repo.reset_Interlocked()
        repo.getAllCookies_Interlocked().fold({ fail() }) {
            assertEquals(it.count(), 0)
        }
    }

    @Test
    fun testDownload() {
        val cookie = HttpCookie("a", "b")
        cookie.domain = "aaa.com"

        jar.cookieStore.add(uriIt.gen(), cookie)

        var secondJar = CustomCookieJar()
        var secondRepo = CookieRepositoryImpl(secondJar)

        secondRepo.downloadFrom_Interlocked(repo)
        repo.getAllCookies_Interlocked().fold({ fail() }) {
            assertEquals(it.count(), 1)
        }
    }
}
