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

package com.fivemin.core.engine.transaction.serialize.postParser

import arrow.core.*
import com.fivemin.core.LoggerController
import com.fivemin.core.TaskDetachedException
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.InitialTransactionImpl
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.toList

class PostParserContentPageImpl<Document : Request>(
    private val pageCondition: PageName,
    private val linkInfoFactory: RequestContentInfoFactory<Document>,
    private val attrInfoFactory: RequestContentInfoFactory<Document>,
    private val inteInfoFactory: InternalContentInfoFactory<Document>,
    private val attributeFactory: DocumentAttributeFactory
) : PostParserContentPage<Document> {
    
    companion object {
        private val logger = LoggerController.getLogger("PostParserContentPageImpl")
    }
    
    override suspend fun extract(
        req: FinalizeRequestTransaction<Document>, info: TaskInfo, state: SessionStartedState
    ): Deferred<Option<List<DocumentAttribute>>> {
        return coroutineScope {
            async {
                try {
    
                    req.previous.ifDocumentAsync({
                        if (it.parseOption.name == pageCondition) {
                            val internals = processIntAttribute(req)
                            val externals = processExtAttr(req, info, state)
                            val links = processLinks(req, info, state)
            
                            externals.plus(links).awaitAll() // early exits if at least one links returns an exception.
            
                            val finished = externals.map { y ->
                                y.await() // awaits already awaited values. orders are not important
                            }
            
                            val ret = internals.fold({ finished }) { x ->
                                finished.plus(x)
                            }
            
                            ret.toOption()
                        } else {
                            none()
                        }
                    }, {
                        none()
                    })
                } catch (e: Exception) {
                    logger.debug(e, "failed to extract")
                    throw e
                }
            }
        }
    }
    
    private suspend fun processIntAttribute(req: FinalizeRequestTransaction<Document>): Option<Iterable<DocumentAttribute>> {
        return inteInfoFactory.get(req).map {
            it.map { x ->
                if (!x.data.any()) {
                    logger.warn(req.request.getDebugInfo() + " < " + x.attributeName + " < has no content; ignoring")
                    none()
                } else if (x.data.count() == 1) {
                    attributeFactory.getInternal(DocumentAttributeInfo(x.attributeName), x.data[0]).orNull().toOption()
                } else {
                    attributeFactory.getInternal(DocumentAttributeInfo(x.attributeName), x.data).orNull().toOption()
                }
            }.filterOption()
        }
    }
    
    private suspend fun downloadAttributes(
        requestLinkInfo: RequestLinkInfo,
        request: HttpRequest,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, FinalizeRequestTransaction<HttpRequest>>> {
        val task = info.createTask<HttpRequest>()
            .get2<InitialTransaction<HttpRequest>, PrepareTransaction<HttpRequest>, FinalizeRequestTransaction<HttpRequest>>(
                DocumentType.NATIVE_HTTP
            )
        
        return state.getChildSession {
            task.start(
                InitialTransactionImpl(requestLinkInfo.option, TagRepositoryImpl(), request), info, it
            )
        }
    }
    
    private suspend fun processExtAttr(
        req: FinalizeRequestTransaction<Document>, info: TaskInfo, state: SessionStartedState
    ): List<Deferred<DocumentAttribute>> {
        val attr = attrInfoFactory.get(req)
        
        val result = attr.linkInfo.map { requestLinkInfo ->
            val downloaded = requestLinkInfo.requests.map { httpRequest ->
                downloadAttributes(requestLinkInfo, httpRequest, info, state)
            }
            
            val list = if (downloaded.any()) {
                Some(finalizeAttr(requestLinkInfo, downloaded))
            } else {
                logger.warn(req.request.getDebugInfo() + " < " + requestLinkInfo.name + " < has no content; ignoring")
                none()
            }
            
            list
        }.filterOption().toList()
        
        return result
    }
    
    private suspend fun finalizeAttr(
        x: RequestLinkInfo, ret: Iterable<Deferred<Either<Throwable, FinalizeRequestTransaction<HttpRequest>>>>
    ): Deferred<DocumentAttribute> {
        return coroutineScope {
            async {
                val finished = coroutineScope {
                    ret.map {
                        async {
                            it.await().fold({ throw it }, ::identity) // early exits if exception raised
                        }
                    }
                }.awaitAll()
                
                val info = DocumentAttributeInfo(x.name)
                
                if (finished.count() == 1) {
                    attributeFactory.getExternal(info, finished[0])
                } else {
                    attributeFactory.getExternal(info, finished)
                }.fold({ throw it }, ::identity)
            }
        }
    }
    
    private suspend fun processLinks(
        request: FinalizeRequestTransaction<Document>, info: TaskInfo, state: SessionStartedState
    ): List<Deferred<Either<Throwable, ExportTransaction<HttpRequest>>>> {
        val links = linkInfoFactory.get(request)
        return links.linkInfo.map { requestLinkInfo ->
            val ret = requestLinkInfo.requests.map { request ->
                downloadLinks(requestLinkInfo, request, info, state)
            }
            
            if (ret.any()) {
                ret.toOption()
            } else {
                logger.warn(request.request.getDebugInfo() + " < " + requestLinkInfo.name + " < has no content; ignoring")
                none()
            }
        }.filterOption().flatten().toList()
    }
    
    private suspend fun downloadLinks(
        requestLinkInfo: RequestLinkInfo,
        request: HttpRequest,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, ExportTransaction<HttpRequest>>> {
        val task = info.createTask<HttpRequest>()
            .get4<InitialTransaction<HttpRequest>, PrepareTransaction<HttpRequest>, FinalizeRequestTransaction<HttpRequest>, SerializeTransaction<HttpRequest>, ExportTransaction<HttpRequest>>(
                DocumentType.NATIVE_HTTP
            )
        
        val ret = state.getChildSession {
            task.start(InitialTransactionImpl(requestLinkInfo.option, TagRepositoryImpl(), request), info, it)
        }
        
        return coroutineScope {
            async {
                try {
                    ret.await()
                } catch (e: TaskDetachedException) {
                    e.left()
                }
            }
        }
    }
}
