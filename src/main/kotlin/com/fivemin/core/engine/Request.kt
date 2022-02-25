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

import arrow.core.Option
import arrow.core.none
import java.net.URI

interface Taggable {
    val tags: TagRepository
}

interface Request : Taggable {
    val token: RequestToken
    val parent: Option<RequestToken>
    val target: URI
    val requestType: RequestType
    val documentType: DocumentType
    
    /**
     * Copies request data to new request with modification.
     * Note that type of copy is same with source.
     */
    fun copyWith(newTarget: Option<URI> = none(), tags: Option<TagRepository> = none()): Request

    fun getDebugInfo(): String {
        return "[" + token.tokenNumber + "]: " + target.toString()
    }
}

class DefaultRequest(
    override val tags: TagRepository,
    override val parent: Option<RequestToken>,
    override val target: URI,
    override val requestType: RequestType,

) : Request {
    override val token: RequestToken = RequestToken.create()
    override val documentType: DocumentType = DocumentType.DEFAULT

    override fun copyWith(newTarget: Option<URI>, newtags: Option<TagRepository>): Request {
        return DefaultRequest(
            newtags.fold({ tags }, { it }),
            parent,
            newTarget.fold({ target }, { it }),
            requestType
        )
    }
}

interface HttpRequest : Request {
    val headerOption: PerRequestHeaderProfile
}

enum class RequestType {
    LINK, ATTRIBUTE
}

enum class DocumentType {
    DEFAULT, NATIVE_HTTP
}
