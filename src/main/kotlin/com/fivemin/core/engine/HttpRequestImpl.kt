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
import java.net.URI

class HttpRequestImpl(
    override val parent: Option<RequestToken>,
    override val target: URI,
    override val requestType: RequestType,
    override val headerOption: PerRequestHeaderProfile,
    override val tags: TagRepository
) : HttpRequest {
    override val token: RequestToken = RequestToken.create()
    override val documentType: DocumentType = DocumentType.NATIVE_HTTP

    override fun copyWith(newTarget: Option<URI>, newTags: Option<TagRepository>): HttpRequest {
        return HttpRequestImpl(
            parent,
            newTarget.fold({ target }, { it }),
            requestType,
            headerOption,
            newTags.fold({ tags }, { it })
        )
    }
}
