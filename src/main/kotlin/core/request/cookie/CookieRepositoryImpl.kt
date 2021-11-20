package core.request.cookie

import arrow.core.Validated
import arrow.core.valid
import java.net.HttpCookie
import java.net.URI

class CookieRepositoryImpl (private val container : CustomCookieJar): CookieRepository {

    private val lock : Any = Any()

    override fun getAllCookies(): Validated<Throwable, Iterable<HttpCookie>> {
        return container.cookieStore.cookies.valid()
    }

    override fun reset() {
        container.cookieStore.removeAll()
    }

    override fun download(repo: CookieRepository) {
        if(repo == this){
            return
        }

        var src = getAllCookies()
        var dst = repo.getAllCookies()

        reset()
        src.map{ it ->
            it.forEach {
                container.cookieStore.add(URI(it.domain), it)
            }
        }
    }

    fun register(act : (CustomCookieJar) -> Unit){
        act(container)
    }

}