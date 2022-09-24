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

package com.fivemin.core.request.adapter

import arrow.core.Either
import arrow.core.flatten
import arrow.core.getOrElse
import arrow.core.toOption
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.HttpRequest
import com.fivemin.core.engine.PerRequestHeaderProfile
import com.fivemin.core.request.RequestHeaderProfile
import com.fivemin.core.request.RequesterAdapter
import com.fivemin.core.request.TaskWaitHandle
import com.fivemin.core.request.cookie.CookieRepository
import com.fivemin.core.request.cookie.CookieRepositoryImpl
import com.fivemin.core.request.cookie.CustomCookieJar
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import ru.gildor.coroutines.okhttp.await
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * RequesterAdapter for okhttp
 *
 * Uses cookie, doesn't follow any redirects.
 */
class RequesterAdapterImpl(
    cookieJar: CustomCookieJar,
    private val responseAdapterImpl: ResponseAdapterImpl,
    private val requesterHeaderProfile: RequestHeaderProfile
) : RequesterAdapter {
    val client: OkHttpClient
    
    companion object {
        private val logger = LoggerController.getLogger("RequesterAdapterImpl")
    }
    
    override val cookieRepository: CookieRepository
    
    init {
        val builder = OkHttpClient.Builder()
        cookieRepository = CookieRepositoryImpl(cookieJar)
        
        client = builder.cookieJar(cookieJar)
            // .bypassInvalidCA()
            .followRedirects(false).followSslRedirects(false).build()
    }
    
    override suspend fun requestAsync(uri: com.fivemin.core.engine.Request): Deferred<Either<Throwable, com.fivemin.core.engine.ResponseBody>> {
        val waiter = TaskWaitHandle<Either<Throwable, com.fivemin.core.engine.ResponseBody>>()
        val ret = waiter.runAsync({
            println("1")
            requestInternal(uri) {
                println("2")
                waiter.registerResult(it)
                println("3")
            }
        }, {
            println("4")
            val cancel = client.dispatcher.runningCalls().singleOrNull {
                it.request().url.toUri().equals(uri)
            }
            
            if (cancel != null) {
                cancel.cancel()
                logger.info("$uri < canceled")
            }
        })
        println("5")
        return ret
    }
    
    private suspend fun <T> requestInternal(
        uri: com.fivemin.core.engine.Request,
        act: suspend (Either<Throwable, com.fivemin.core.engine.ResponseBody>) -> T
    ) {
        coroutineScope {
            launch {
                val request = Request.Builder()
                
                request.url(uri.target.toURL())
                request.get()
                
                if (uri is HttpRequest) {
                    request.setHeader(uri.headerOption, requesterHeaderProfile)
                } else {
                    request.setHeader(requesterHeaderProfile)
                }
                
                val requesterBuilt = request.build()
                val result = Either.catch {
                    logger.debug(requesterBuilt.url.toString() + " < requesting")
                    val call = client.newCall(requesterBuilt)
                    val handle = TaskWaitHandle<Response>()
                    handle.runAsync({
                        handle.registerResult(call.execute())
                    }, {
                        call.cancel()
                    }).await()
                }.fold({
                    logger.info(requesterBuilt.url.toString() + " < received")
                    logger.warn(it)
                    
                    if (it.message?.lowercase()?.contains("canceled") == true) {
                        return@fold Either.catch {
                            responseAdapterImpl.createWithCanceled(uri, requesterBuilt)
                        }.flatten()
                    }
                    
                    Either.catch {
                        responseAdapterImpl.createWithError(uri, it.toOption(), requesterBuilt)
                    }.flatten()
                }, {
                    logger.info(requesterBuilt.url.toString() + " < received")
                    
                    Either.catch {
                        responseAdapterImpl.createWithReceived(uri, it, requesterBuilt)
                    }.flatten()
                })
                act(result)
            }
        }
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
    
    private fun Request.Builder.setHeader(
        headerOption: PerRequestHeaderProfile, backupProfile: RequestHeaderProfile
    ): Request.Builder {
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
    
    private fun OkHttpClient.Builder.bypassInvalidCerificate(): OkHttpClient.Builder {
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
        
        this.sslSocketFactory(sslContext.socketFactory, trust[0] as X509TrustManager).hostnameVerifier { _, _ -> true }
        
        return this
    }
}
