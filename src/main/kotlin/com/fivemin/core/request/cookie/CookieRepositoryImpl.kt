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
