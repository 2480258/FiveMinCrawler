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

import com.fivemin.core.engine.*

fun <Return, Document : Request> PrepareTransaction<Document>.ifDocument(
    document: (PrepareDocumentTransaction<Document>) -> Return,
    other: (PrepareTransaction<Document>) -> Return
): Return {
    return if (this is PrepareDocumentTransaction<Document>) {
        document(this)
    } else {
        other(this)
    }
}
suspend fun <Return, Document : Request> PrepareTransaction<Document>.ifDocumentAsync(
    document: suspend (PrepareDocumentTransaction<Document>) -> Return,
    other: suspend (PrepareTransaction<Document>) -> Return
): Return {
    return if (this is PrepareDocumentTransaction<Document>) {
        document(this)
    } else {
        other(this)
    }
}

interface PrepareTransaction<out Document : Request> : ReverableTransaction<InitialTransaction<Request>, Document> {
    val requestOption: RequestOption
}

interface PrepareDocumentTransaction<out Document : Request> :
    PrepareTransaction<Document> {
    val parseOption: ParseOption
    val containerOption: ContainerOption
}
