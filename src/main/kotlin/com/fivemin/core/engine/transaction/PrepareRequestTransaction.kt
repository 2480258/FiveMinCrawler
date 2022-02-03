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

package com.fivemin.core.engine.transaction

import com.fivemin.core.engine.*

data class PrepareDocumentRequestTransactionImpl<out Document : Request>(
    override val previous: Transaction<Document>,
    override val tags: TagRepository,
    override val requestOption: RequestOption,
    override val parseOption: ParseOption,
    override val containerOption: ContainerOption
) : PrepareDocumentTransaction<Document> {
    override val request: Document = previous.request

    fun modifyTags(tags: TagRepository): Taggable {
        return PrepareDocumentRequestTransactionImpl(previous, tags, requestOption, parseOption, containerOption)
    }
}

data class PrepareRequestTransactionImpl<Document : Request>(
    override val previous: Transaction<Document>,
    override val tags: TagRepository,
    override val requestOption: RequestOption
) : PrepareTransaction<Document> {

    override val request: Document
        get() = previous.request

    fun modifyTags(tags: TagRepository): Taggable {
        return PrepareRequestTransactionImpl(previous, tags, requestOption)
    }
}
