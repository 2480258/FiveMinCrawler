package com.fivemin.core

import arrow.core.*
import com.fivemin.core.AttributeMockFactory.Companion.asMultiUpgrade
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.export.ExportAttributeInfo
import com.fivemin.core.engine.transaction.export.ExportAttributeLocator
import com.fivemin.core.engine.transaction.finalizeRequest.DocumentRequest
import com.fivemin.core.engine.transaction.finalizeRequest.DocumentRequestInfo
import com.fivemin.core.parser.HtmlDocumentFactoryImpl
import com.fivemin.core.request.*
import io.mockk.every
import io.mockk.mockk
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URI

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
        
        fun getSingleStringAttr(
            name: String,
            value: String
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
            name: String,
            value: Iterable<String>
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
            engine: String? = null,
            slot: Int? = null
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
                RequesterPreference(
                    RequesterEngineInfo(engine ?: "Default"),
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
            } returns ((resp ?: this.upgradeAsRequestReq().upgrade().getSuccResponse()).right())
            
            return ret
        }
        
        fun InitialTransaction<Request>.upgradeAsDocument(
            name: String,
            mode: WorkingSetMode = WorkingSetMode.Enabled,
            engine: String? = null,
            slot: Int? = null
        ): PrepareDocumentTransaction<Request> {
            //if (this.request.requestType != RequestType.LINK) {
            //   throw IllegalArgumentException()
            //}
            
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
                RequesterPreference(
                    RequesterEngineInfo(engine ?: "Default"),
                    slot.toOption().map { RequesterSlotInfo(it) })
            ))
            
            every {
                ret.containerOption
            } returns (ContainerOption(mode))
            
            every {
                ret.parseOption
            } returns (ParseOption(PageName(name)))
            
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
            } returns ((resp ?: this.upgradeAsRequestReq().upgrade().getSuccResponse()).right())
            
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
                        this.request.requestOption.preference.engine,
                        RequesterSlotInfo(0)
                    ), ffac
                ))
            }) {
                every {
                    ret.info
                } returns (PreprocessRequestInfo(
                    PerformedRequesterInfo(
                        this.request.requestOption.preference.engine,
                        it
                    ), ffac
                ))
            }
            
            return ret
        }
        
        fun PreprocessedRequest<Request>.getSuccResponse(
            content: String? = null,
            time: ResponseTime? = null
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
            uri: URI,
            type: RequestType,
            parent: RequestToken? = null,
            tags: TagRepository? = null
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
            uri: URI,
            type: RequestType,
            parent: RequestToken? = null,
            tags: TagRepository? = null
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
            
            return ret
        }
    }
}