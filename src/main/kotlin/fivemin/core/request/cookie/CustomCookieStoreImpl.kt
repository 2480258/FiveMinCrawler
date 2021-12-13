package fivemin.core.request.cookie

import java.net.CookieStore
import java.net.HttpCookie
import java.net.URI

class CustomCookieStoreImpl : CookieStore {
    val list : MutableMap<URI, HttpCookie> = mutableMapOf()

    override fun add(uri: URI?, cookie: HttpCookie?) {
        if(uri != null && cookie != null){
            list[uri] = cookie
        }
    }

    override fun get(uri: URI?): MutableList<HttpCookie> {
        TODO("Not yet implemented")
    }

    override fun getCookies(): MutableList<HttpCookie> {
        TODO("Not yet implemented")
    }

    override fun getURIs(): MutableList<URI> {
        TODO("Not yet implemented")
    }

    override fun remove(uri: URI?, cookie: HttpCookie?): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeAll(): Boolean {
        TODO("Not yet implemented")
    }

}