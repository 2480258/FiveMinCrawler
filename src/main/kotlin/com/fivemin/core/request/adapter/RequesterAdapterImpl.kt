package com.fivemin.core.request.adapter

import arrow.core.*
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.HttpRequest
import com.fivemin.core.engine.PerRequestHeaderProfile
import com.fivemin.core.request.RequestHeaderProfile
import com.fivemin.core.request.RequesterAdapter
import com.fivemin.core.request.TaskWaitHandle
import com.fivemin.core.request.cookie.CustomCookieJar
import kotlinx.coroutines.Deferred
import okhttp3.*
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class RequesterAdapterImpl(cookieJar: CustomCookieJar, private val responseAdapterImpl: ResponseAdapterImpl, private val requesterHeaderProfile: RequestHeaderProfile) :
    RequesterAdapter {
    val client: OkHttpClient

    companion object {
        private val logger = LoggerController.getLogger("RequesterAdapterImpl")
    }

    init {
        var builder = OkHttpClient.Builder()

        client = builder
            .cookieJar(cookieJar)
            // .bypassInvalidCA()
            .followRedirects(false)
            .followSslRedirects(false)
            .build()
    }

    override suspend fun requestAsync(uri: com.fivemin.core.engine.Request): Deferred<Either<Throwable, com.fivemin.core.engine.ResponseBody>> {
        val waiter = TaskWaitHandle<Either<Throwable, com.fivemin.core.engine.ResponseBody>>()

        return waiter.run {
            requestInternal(uri) {
                waiter.registerResult(it)
            }
        }
    }

    private fun <T> requestInternal(uri: com.fivemin.core.engine.Request, act: (Either<Throwable, com.fivemin.core.engine.ResponseBody>) -> T) {
        val request = Request.Builder()

        request.url(uri.target.toURL())
        request.get()

        if (uri is HttpRequest) {
            request.setHeader(uri.headerOption, requesterHeaderProfile)
        } else {
            request.setHeader(requesterHeaderProfile)
        }

        var ret = request.build()

        act(
            Either.catch {
                logger.debug(ret.url.toString() + " < requesting")
                client.newCall(ret).execute()
            }.fold({
                logger.debug(ret.url.toString() + " < received")
                logger.debug(it.stackTraceToString())

                Either.catch {
                    responseAdapterImpl.createWithError(uri, it.toOption(), ret)
                }.flatten()
            }, {
                logger.debug(ret.url.toString() + " < received")

                Either.catch {
                    responseAdapterImpl.createWithReceived(uri, it, ret)
                }.flatten()
            })
        )
    }

    private fun Request.Builder.setHeader(headerOption: RequestHeaderProfile): Request.Builder {

        headerOption.userAgent.map {
            this.header("User-Agent", it)
        }

        headerOption.acceptEncoding.map {
            this.header("Accept-Encoding", it)
        }

        headerOption.connection.map {
            this.header("Connection", it)
        }

        headerOption.te.map {
            this.header("TE", it)
        }

        headerOption.acceptLanguage.map {
            this.header("Accept-Language", it)
        }

        return this
    }

    private fun Request.Builder.setHeader(headerOption: PerRequestHeaderProfile, backupProfile: RequestHeaderProfile): Request.Builder {
        val option = headerOption.requestHeaderProfile.getOrElse { backupProfile }

        option.userAgent.map {
            this.header("User-Agent", it)
        }

        headerOption.accept.map {
            this.header("Accept", it)
        }

        option.acceptEncoding.map {
            this.header("Accept-Encoding", it)
        }

        option.connection.map {
            this.header("Connection", it)
        }

        option.te.map {
            this.header("TE", it)
        }

        option.acceptLanguage.map {
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
