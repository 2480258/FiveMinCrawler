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

package com.fivemin.core.engine

import arrow.core.*
import com.fivemin.core.LoggerController
import java.net.URI

fun <T> DocumentAttributeElement.match(ifInte: (DocumentAttributeInternalElement) -> T, ifExt: (DocumentAttributeExternalElement) -> T): T {
    if (this is DocumentAttributeInternalElement) {
        return ifInte(this)
    }
    if (this is DocumentAttributeExternalElement) {
        return ifExt(this)
    }

    throw IllegalArgumentException()
}

interface DocumentAttributeFactory {
    suspend fun getInternal(info: DocumentAttributeInfo, data: String): Either<Throwable, DocumentAttribute>
    suspend fun getInternal(info: DocumentAttributeInfo, data: Iterable<String>): Either<Throwable, DocumentAttribute>
    suspend fun <Document : Request> getExternal(info: DocumentAttributeInfo, data: FinalizeRequestTransaction<Document>): Either<Throwable, DocumentAttribute>
    suspend fun <Document : Request> getExternal(info: DocumentAttributeInfo, data: Iterable<FinalizeRequestTransaction<Document>>): Either<Throwable, DocumentAttribute>
}

class NoAttributeContentException : Exception()

class DocumentAttributeFactoryImpl : DocumentAttributeFactory {

    companion object {
        private val logger = LoggerController.getLogger("DocumentAttributeFactoryImpl")
    }

    private suspend fun create(data: String): DocumentAttributeInternalElement {
        return DocumentAttributeInternalElementImpl(data)
    }

    private suspend fun <Document : Request> create(data: FinalizeRequestTransaction<Document>): Either<Throwable, DocumentAttributeExternalElement> {
        return data.result.map {
            it.responseBody.ifSuccAsync({
                DocumentAttributeExternalElementImpl(data.request.token, data.request.target, data.tags, data.previous.requestOption, it).right()
            }, {
                IllegalArgumentException().left()
            })
        }.flatten()
    }

    override suspend fun getInternal(info: DocumentAttributeInfo, data: String): Either<Throwable, DocumentAttribute> {
        val item = DocumentAttributeSingleItemImpl(create(data))
        return DocumentAttributeImpl(item, info).right()
    }

    override suspend fun getInternal(info: DocumentAttributeInfo, data: Iterable<String>): Either<Throwable, DocumentAttribute> {
        if (!data.any()) {
            return NoAttributeContentException().left()
        }

        if (data.count() == 1) {
            return getInternal(info, data.first())
        }

        val items = DocumentAttributeArrayItemImpl(
            data.map {
                create(it)
            }
        )

        return DocumentAttributeImpl(items, info).right()
    }

    override suspend fun <Document : Request> getExternal(
        info: DocumentAttributeInfo,
        data: FinalizeRequestTransaction<Document>
    ): Either<Throwable, DocumentAttribute> {

        val item = create(data).map {
            DocumentAttributeSingleItemImpl(it)
        }

        return item.map {
            DocumentAttributeImpl(it, info)
        }
    }

    override suspend fun <Document : Request> getExternal(
        info: DocumentAttributeInfo,
        data: Iterable<FinalizeRequestTransaction<Document>>
    ): Either<Throwable, DocumentAttribute> {

        val ret = data.map {
            val r = create(it)

            r.swap().map { x ->
                logger.warn(it.request.getDebugInfo() + " < can't extract attribute from due to: " + x)
            }

            r.orNull().toOption()
        }.filterOption()

        val item = DocumentAttributeArrayItemImpl(ret)

        if (!ret.any()) {
            return NoAttributeContentException().left()
        }

        if (ret.count() == 1) {
            val single = DocumentAttributeSingleItemImpl(ret.first())
            return DocumentAttributeImpl(single, info).right()
        }

        return DocumentAttributeImpl(item, info).right()
    }
}

data class DocumentAttributeInfo(val name: String)

interface DocumentAttribute {
    val item: DocumentAttributeItem
    val info: DocumentAttributeInfo
}

class DocumentAttributeImpl(override val item: DocumentAttributeItem, override val info: DocumentAttributeInfo) : DocumentAttribute

interface DocumentAttributeItem : Iterable<DocumentAttributeElement>

interface DocumentAttributeElement

interface DocumentAttributeInternalElement : DocumentAttributeElement {
    val body: String
}

interface DocumentAttributeExternalElement : DocumentAttributeElement {
    val handle: RequestToken
    val target: URI
    val tagRepo: TagRepository
    val requestOption: RequestOption
    val successInfo: SuccessBody
}

interface DocumentAttributeSingleItem : DocumentAttributeItem {
    val elem: DocumentAttributeElement
}

class DocumentAttributeExternalElementImpl(
    override val handle: RequestToken,
    override val target: URI,
    override val tagRepo: TagRepository,
    override val requestOption: RequestOption,
    override val successInfo: SuccessBody
) : DocumentAttributeExternalElement

class DocumentAttributeSingleItemImpl(override val elem: DocumentAttributeElement) : DocumentAttributeSingleItem {
    override fun iterator(): Iterator<DocumentAttributeElement> {
        return listOf(elem).listIterator()
    }
}

interface DocumentAttributeArrayItem : DocumentAttributeItem {
    val elem: Iterable<DocumentAttributeElement>
}

class DocumentAttributeArrayItemImpl(override val elem: Iterable<DocumentAttributeElement>) : DocumentAttributeArrayItem {
    override fun iterator(): Iterator<DocumentAttributeElement> {
        return elem.iterator()
    }
}

class DocumentAttributeInternalElementImpl(
    override val body: String
) : DocumentAttributeInternalElement
