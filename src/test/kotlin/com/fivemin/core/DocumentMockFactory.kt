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

package com.fivemin.core

import arrow.core.*
import com.fivemin.core.DocumentMockFactory.Companion.upgrade
import com.fivemin.core.DocumentMockFactory.Companion.upgradeAsDocument
import com.fivemin.core.engine.*
import com.fivemin.core.engine.crawlingTask.DocumentPolicyStorageFactory
import com.fivemin.core.engine.crawlingTask.DocumentPolicyStorageFactoryCollector
import com.fivemin.core.engine.transaction.StringUniqueKeyProvider
import com.fivemin.core.engine.transaction.UriUniqueKeyProvider
import com.fivemin.core.engine.transaction.export.ExportAttributeInfo
import com.fivemin.core.engine.transaction.export.ExportAttributeLocator
import com.fivemin.core.engine.transaction.finalizeRequest.DocumentRequest
import com.fivemin.core.engine.transaction.finalizeRequest.DocumentRequestInfo
import com.fivemin.core.parser.HtmlDocumentFactoryImpl
import com.fivemin.core.request.DequeueDecision
import com.fivemin.core.request.DequeueDecisionFactory
import com.fivemin.core.request.PreprocessRequestInfo
import com.fivemin.core.request.PreprocessedRequest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import java.io.InputStream
import java.io.InputStreamReader
import java.io.InvalidObjectException
import java.net.URI

class StubMockFactory {
    companion object {
        fun mockState(): SessionStartedState {
            val state: SessionStartedState = mockk(relaxed = true)
            val res: Deferred<Either<Throwable, Any>> = mockk()
            
            every {
                state.addAlias(any())
            } returns (Unit)
            
            coEvery {
                state.retryAsync(any<suspend (SessionInitState) -> Deferred<Either<Throwable, Any>>>())
            } returns (res)
            
            coEvery {
                res.await()
            } returns (mockk())
            
            coEvery {
                state.getChildSession<Any>(any())
            } coAnswers {
                coroutineScope {
                    async {
                        Either.catch {
                            DocumentMockFactory.getRequest(URI("https://aaa.com"), RequestType.LINK).upgrade().upgradeAsDocument("a").upgrade()
                        }
                    }
                }
            }
            
            return state
        }
        
        fun mockInfo(): TaskInfo {
            val provider = KeyProvider(UriUniqueKeyProvider(), StringUniqueKeyProvider())
            val taskInfo: TaskInfo = mockk(relaxed = true)
            
            val createdTaskFac: CrawlerTaskFactory<Request> = mockk(relaxed = true)
            val createdTask: CrawlerTask2<InitialTransaction<Request>,
                    PrepareTransaction<Request>,
                    FinalizeRequestTransaction<Request>, Request, Request, Request> =
                mockk(relaxed = true)
            
            coEvery {
                createdTask.start(any(), any(), any())
            } coAnswers {
                coroutineScope {
                    async {
                        Either.catch {
                            DocumentMockFactory.getRequest(URI("https://aa.com"), RequestType.LINK).upgrade()
                                .upgradeAsDocument("a")
                                .upgrade()
                        }
                    }
                }
            }
            
            every {
                taskInfo.createTask<Request>()
            } returns (createdTaskFac)
            
            every {
                createdTaskFac.get2<InitialTransaction<Request>,
                        PrepareTransaction<Request>,
                        FinalizeRequestTransaction<Request>>(any())
            } answers {
                createdTask
            }
            
            every {
                taskInfo.uniqueKeyProvider
            } returns (provider)
            
            return taskInfo
        }
    }
}

class AttributeMockFactory {
    companion object {
        
        fun DocumentAttribute.asSingleUpgrade(tag: TagRepository? = null): ExportAttributeInfo {
            val pair = Pair(this.item.first(), ExportAttributeLocator(this.info, none()))
            val info = ExportAttributeInfo(pair.second, pair.first, tag ?: TagRepositoryImpl())
            
            return info
        }
        
        fun DocumentAttribute.asMultiUpgrade(tag: TagRepository? = null): Iterable<ExportAttributeInfo> {
            return this.item.mapIndexed { x, y ->
                Pair(y, ExportAttributeLocator(this.info, x.toOption()))
            }.map {
                ExportAttributeInfo(it.second, it.first, tag ?: TagRepositoryImpl())
            }
        }
        
        fun getMultiStringAttrEx(
            name: String, triple: Iterable<Triple<Request, RequestOption, SuccessBody>>
        ): DocumentAttribute {
            val mock = mockk<DocumentAttribute>()
            
            every {
                mock.info
            }.returns(DocumentAttributeInfo(name))
            
            every {
                mock.item
            }.returns(DocumentAttributeArrayItemImpl(triple.map {
                DocumentAttributeExternalElementImpl(
                    it.first.token, it.first.target, it.first.tags, it.second, it.third
                )
            }))
            
            return mock
        }
        
        fun getSingleStringAttrEx(
            name: String, req: Request, succ: SuccessBody, opt: RequestOption
        ): DocumentAttribute {
            val mock = mockk<DocumentAttribute>()
            
            every {
                mock.info
            }.returns(DocumentAttributeInfo(name))
            
            every {
                mock.item
            }.returns(
                DocumentAttributeSingleItemImpl(
                    DocumentAttributeExternalElementImpl(
                        req.token, req.target, req.tags, opt, succ
                    )
                )
            )
            
            return mock
        }
        
        fun getSingleStringAttr(
            name: String, value: String
        ): DocumentAttribute {
            val mock = mockk<DocumentAttribute>()
            
            every {
                mock.info
            }.returns(DocumentAttributeInfo(name))
            
            every {
                mock.item
            }.returns(DocumentAttributeSingleItemImpl(DocumentAttributeInternalElementImpl(value)))
            
            return mock
        }
        
        fun getMultiSingleAttr(
            name: String, value: Iterable<String>
        ): DocumentAttribute {
            val mock = mockk<DocumentAttribute>()
            
            every {
                mock.info
            }.returns(DocumentAttributeInfo(name))
            
            every {
                mock.item
            }.returns(DocumentAttributeArrayItemImpl(value.map {
                DocumentAttributeInternalElementImpl(it)
            }))
            
            return mock
        }
    }
}

class DocumentMockFactory {
    companion object {
        
        fun Request.upgrade(): InitialTransaction<Request> {
            val ret = mockk<InitialTransaction<Request>>()
            
            every {
                ret.tags
            } returns (this.tags)
            
            every {
                ret.request
            } returns (this)
            
            every {
                ret.option
            } returns (InitialOption())
            
            return ret
        }
        
        fun InitialTransaction<Request>.upgradeAsAttribute(
            engine: String? = null, slot: Int? = null
        ): PrepareTransaction<Request> {
            if (this.request.requestType == RequestType.LINK) {
                throw IllegalArgumentException()
            }
            
            val ret = mockk<PrepareTransaction<Request>>()
            
            every {
                ret.tags
            } returns (this.tags)
            
            every {
                ret.request
            } returns (this.request)
            
            every {
                ret.previous
            } returns (this)
            
            every {
                ret.requestOption
            } returns (RequestOption(
                RequesterPreference(RequesterEngineInfo(engine ?: "Default"),
                    slot.toOption().map { RequesterSlotInfo(it) })
            ))
            
            return ret
        }
        
        fun PrepareTransaction<Request>.upgradeAsRequestReq(detachableState: DetachableState? = null): DocumentRequest<Request> {
            val ret = mockk<DocumentRequest<Request>>()
            
            every {
                ret.request
            } returns (this)
            
            every {
                ret.info
            } returns (DocumentRequestInfo(detachableState ?: DetachableState.WANT))
            
            return ret
        }
        
        fun PrepareDocumentTransaction<Request>.upgrade(resp: ResponseData? = null): FinalizeRequestTransaction<Request> {
            val ret = mockk<FinalizeRequestTransaction<Request>>()
            
            every {
                ret.tags
            } returns (this.tags)
            
            every {
                ret.request
            } returns (this.request)
            
            every {
                ret.previous
            } returns (this)
            
            every {
                ret.result
            } returns (Either.catch { (resp ?: this.upgradeAsRequestReq().upgrade().getSuccResponse_Html()) })
            
            return ret
        }
        
        fun InitialTransaction<Request>.upgradeAsDocument(
            pageName: String, mode: WorkingSetMode = WorkingSetMode.Enabled, engine: String? = null, slot: Int? = null
        ): PrepareDocumentTransaction<Request> {
            // if (this.request.requestType != RequestType.LINK) {
            //   throw IllegalArgumentException()
            // }
            
            val ret = mockk<PrepareDocumentTransaction<Request>>()
            
            every {
                ret.tags
            } returns (this.tags)
            
            every {
                ret.request
            } returns (this.request)
            
            every {
                ret.previous
            } returns (this)
            
            every {
                ret.requestOption
            } returns (RequestOption(
                RequesterPreference(RequesterEngineInfo(engine ?: "Default"),
                    slot.toOption().map { RequesterSlotInfo(it) })
            ))
            
            every {
                ret.containerOption
            } returns (ContainerOption(mode))
            
            every {
                ret.parseOption
            } returns (ParseOption(PageName(pageName)))
            
            return ret
        }
        
        fun PrepareTransaction<Request>.upgrade(resp: ResponseData? = null): FinalizeRequestTransaction<Request> {
            val ret = mockk<FinalizeRequestTransaction<Request>>()
            
            every {
                ret.tags
            } returns (this.tags)
            
            every {
                ret.request
            } returns (this.request)
            
            every {
                ret.previous
            } returns (this)
            
            every {
                ret.result
            } returns ((resp ?: this.upgradeAsRequestReq().upgrade().getSuccResponse_Html()).right())
            
            return ret
        }
        
        fun FinalizeRequestTransaction<Request>.upgrade(attr: Iterable<DocumentAttribute>? = null): SerializeTransaction<Request> {
            val ret = mockk<SerializeTransaction<Request>>()
            
            every {
                ret.tags
            } returns (this.tags)
            
            every {
                ret.request
            } returns (this.request)
            
            every {
                ret.attributes
            } returns (attr ?: listOf())
            
            return ret
        }
        
        fun SerializeTransaction<Request>.upgrade(handles: List<Either<Throwable, ExportResultToken>>? = null): ExportTransaction<Request> {
            val ret = mockk<ExportTransaction<Request>>()
            
            var fHandles = handles
            
            if (handles == null) {
                fHandles = listOf()
            }
            
            every {
                ret.tags
            } returns (this.tags)
            
            every {
                ret.request
            } returns (this.request)
            
            every {
                ret.exportHandles
            } returns (fHandles!!)
            
            return ret
        }
        
        fun PrepareDocumentTransaction<Request>.upgradeAsRequestDoc(): DocumentRequest<Request> {
            val ret = mockk<DocumentRequest<Request>>()
            
            every {
                ret.request
            } returns (this)
            
            //This is actually not 100% correct, but almost correct
            val info =
                DocumentRequestInfo(if (this.containerOption.workingSetMode == WorkingSetMode.Enabled) DetachableState.WANT else DetachableState.HATE)
            
            every {
                ret.info
            } returns (info)
            
            return ret
        }
        
        fun DocumentRequest<Request>.upgrade(fac: DequeueDecisionFactory? = null): PreprocessedRequest<Request> {
            val ret = mockk<PreprocessedRequest<Request>>()
            
            var ffac: DequeueDecisionFactory? = fac
            
            if (ffac == null) {
                val facMock = mockk<DequeueDecisionFactory>()
                
                every {
                    facMock.get()
                } returns (DequeueDecision.ALLOW)
                
                ffac = facMock
            }
            
            every {
                ret.request
            } returns (this)
            
            this.request.requestOption.preference.slot.fold({
                every {
                    ret.info
                } returns (PreprocessRequestInfo(
                    PerformedRequesterInfo(
                        this.request.requestOption.preference.engine, RequesterSlotInfo(0)
                    ), ffac
                ))
            }) {
                every {
                    ret.info
                } returns (PreprocessRequestInfo(
                    PerformedRequesterInfo(
                        this.request.requestOption.preference.engine, it
                    ), ffac
                ))
            }
            
            return ret
        }
        
        fun PreprocessedRequest<Request>.getCriticalBodyResponse(): ResponseData {
            val bdy = mockk<ResponseData>()
            val result = mockk<CriticalErrorBody>()
            
            every {
                bdy.responseBody
            } returns (result)
            
            return bdy
        }
        
        fun PreprocessedRequest<Request>.getSuccResponse_Html(
            content: String? = null, time: ResponseTime? = null
        ): ResponseData {
            val cont = content.orEmpty()
            val resultMock = mockk<ResponseData>()
            val data = mockk<HtmlMemoryData>()
            
            val by = Charsets.UTF_8.encode(cont).array().inputStream()
            
            every {
                data.openStreamAsByteAndDispose {
                    any<Any>()
                    
                    Any()
                }
            } answers {
                this.firstArg<(InputStream) -> Any>()(by).right()
            }
            
            every {
                data.openStreamAsStringAndDispose {
                    any<Any>()
                    
                    Any()
                }
            } answers {
                this.firstArg<(InputStreamReader) -> Any>()(InputStreamReader(by)).right()
            }
            
            val f = HtmlDocumentFactoryImpl()
            
            every {
                data.parseAsHtmlDocument<Any>(any())
            } answers {
                this.firstArg<(HtmlParsable) -> Any>()(f.create(cont)).right()
            }
            
            coEvery {
                data.parseAsHtmlDocumentAsync<Any>(any())
            } coAnswers {
                this.firstArg<suspend (HtmlParsable) -> Any>()(f.create(cont)).right()
            }
            
            val reqq = mockk<RequestBody>()
            
            every {
                reqq.currentUri
            } returns (this.request.request.request.target)
            
            val succ = mockk<SuccessBody>()
            every {
                succ.requestBody
            } returns (reqq)
            
            every {
                succ.responseTime
            } returns (ResponseTime(5, 0))
            
            every {
                succ.body
            } returns (data)
            
            every {
                resultMock.responseBody
            } returns (succ)
            
            every {
                resultMock.requesterInfo
            } returns (this.info.info)
            
            return resultMock
        }
        
        fun getHttpRequest(
            uri: URI, type: RequestType, parent: RequestToken? = null, tags: TagRepository? = null
        ): HttpRequest {
            val ret = mockk<HttpRequest>()
            
            val token = RequestToken.create()
            
            every {
                ret.target
            } returns (uri)
            
            every {
                ret.parent
            } returns (parent.toOption())
            
            every {
                ret.token
            } returns (token)
            
            every {
                ret.tags
            } returns (tags ?: TagRepositoryImpl())
            
            every {
                ret.requestType
            } returns (type)
            
            every {
                ret.documentType
            } returns (DocumentType.DEFAULT)
            
            every {
                ret.getDebugInfo()
            } returns ("[" + token.tokenNumber + "]: " + ret.target.path + (ret.target.query ?: ""))
            
            return ret
        }
        
        fun getRequest(
            uri: URI, type: RequestType, parent: RequestToken? = null, tags: TagRepository? = null
        ): Request {
            val ret = mockk<Request>()
            
            val token = RequestToken.create()
            
            every {
                ret.target
            } returns (uri)
            
            every {
                ret.parent
            } returns (parent.toOption())
            
            every {
                ret.token
            } returns (token)
            
            every {
                ret.tags
            } returns (tags ?: TagRepositoryImpl())
            
            every {
                ret.requestType
            } returns (type)
            
            every {
                ret.documentType
            } returns (DocumentType.DEFAULT)
            
            every {
                ret.getDebugInfo()
            } returns ("[" + token.tokenNumber + "]: " + ret.target.path + (ret.target.query ?: ""))
            
            every {
                ret.copyWith(any())
            } answers {
                val first = firstArg<Option<URI>>()
                
                first.fold({
                    throw InvalidObjectException("")
                }, {
                    getRequest(it, type)
                })
                
            }
            
            return ret
        }
    }
}
