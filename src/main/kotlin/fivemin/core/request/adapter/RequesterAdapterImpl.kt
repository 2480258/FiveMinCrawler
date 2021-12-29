package fivemin.core.request.adapter

import arrow.core.*
import arrow.core.computations.option
import fivemin.core.engine.HttpRequest
import fivemin.core.engine.PerRequestHeaderProfile
import fivemin.core.engine.RequestBody
import fivemin.core.engine.ResponseData
import fivemin.core.request.NetworkHeader
import fivemin.core.request.RequesterAdapter
import fivemin.core.request.TaskWaitHandle
import fivemin.core.request.cookie.CustomCookieJar
import kotlinx.coroutines.Deferred
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import java.net.URI
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class RequesterAdapterImpl(cookieJar: CustomCookieJar, private val responseAdapterImpl: ResponseAdapterImpl) :
    RequesterAdapter {
    val client: OkHttpClient
    init {
        var builder = OkHttpClient.Builder()

        client = builder
            .cookieJar(cookieJar)
            .bypassInvalidCA()
            .followRedirects(false)
            .followSslRedirects(false)
            .build()
    }

    override suspend fun requestAsync(uri: fivemin.core.engine.Request): Deferred<Either<Throwable, fivemin.core.engine.ResponseBody>> {
        val waiter = TaskWaitHandle<Either<Throwable, fivemin.core.engine.ResponseBody>>()

        return waiter.run {
            requestInternal(uri) {
                waiter.registerResult(it)
            }
        }
    }

    private fun <T> requestInternal(uri: fivemin.core.engine.Request, act: (Either<Throwable, fivemin.core.engine.ResponseBody>) -> T) {
        val request = Request.Builder()

        request.url(uri.target.toURL())
        request.get()

        if(uri is HttpRequest){
            request.setHeader(uri.headerOption)
        }

        var ret = request.build()

        client.newCall(ret).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                act(responseAdapterImpl.createWithError(uri, e.toOption(), ret))
            }

            override fun onResponse(call: Call, response: Response) {
                try { //prevent not responding from exception
                    act(responseAdapterImpl.createWithReceived(uri, response, ret))
                } catch (e : Exception) {
                    act(responseAdapterImpl.createWithError(uri, e.toOption(), ret))
                }
            }
        })
    }

    private fun Request.Builder.setHeader(headerOption: PerRequestHeaderProfile): Request.Builder {
        headerOption.requestHeaderProfile.userAgent.map {
            this.header("User-Agent", it)
        }

        headerOption.accept.map {
            this.header("Accept", it)
        }

        headerOption.requestHeaderProfile.acceptEncoding.map {
            this.header("Accept-Encoding", it)
        }

        headerOption.requestHeaderProfile.connection.map {
            this.header("Connection", it)
        }

        headerOption.requestHeaderProfile.te.map {
            this.header("TE", it)
        }

        headerOption.requestHeaderProfile.acceptLanguage.map {
            this.header("Accept-Language", it)
        }

        headerOption.referrer.map {
            this.header("Referrer", it.toASCIIString())
        }

        return this
    }

    private fun OkHttpClient.Builder.bypassInvalidCA(): OkHttpClient.Builder {
        val trust = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trust, SecureRandom())

        this
            .sslSocketFactory(sslContext.socketFactory, trust[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }

        return this
    }

}