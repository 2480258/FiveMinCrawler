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
        repo.getAllCookies().fold({ fail() }) {
            assertEquals(it.count(), 1)
        }
    }

    @Test
    fun testReset() {
        jar.cookieStore.add(uriIt.gen(), HttpCookie("a", "b"))
        repo.reset()
        repo.getAllCookies().fold({ fail() }) {
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

        secondRepo.download(repo)
        repo.getAllCookies().fold({ fail() }) {
            assertEquals(it.count(), 1)
        }
    }
}
