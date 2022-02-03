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

package com.fivemin.core.engine.crawlingTask

import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.export.ExportTransactionPolicy
import com.fivemin.core.engine.transaction.finalizeRequest.FinalizeRequestTransactionPolicy
import com.fivemin.core.engine.transaction.prepareRequest.PrepareRequestTransactionPolicy
import com.fivemin.core.engine.transaction.serialize.SerializeTransactionPolicy
import kotlin.reflect.*

class DocumentPolicyStorageFactory(
    val prepare: PrepareRequestTransactionPolicy<Request>,
    val request: FinalizeRequestTransactionPolicy<Request>,
    val serialize: SerializeTransactionPolicy<Request>,
    val export: ExportTransactionPolicy<Request>
) {
    fun <Document : Request> create(): DocumentTypePolicyStorage<Document> {
        return DocumentTypePolicyStorage(prepare, request, serialize, export)
    }
}

class DocumentTypePolicyStorage<Document : Request>(
    val prepare: PrepareRequestTransactionPolicy<Request>,
    val request: FinalizeRequestTransactionPolicy<Request>,
    val serialize: SerializeTransactionPolicy<Request>,
    val export: ExportTransactionPolicy<Request>
) {

    inline fun <reified SrcTrans : Transaction<Document>, reified DstTrans : StrictTransaction<SrcTrans, Document>> find(): TransactionPolicy<SrcTrans, DstTrans, Document, Document> {
        var r = SrcTrans::class.qualifiedName?.lowercase()

        if (r != null) {
            if (r.contains("init")) {
                return prepare as TransactionPolicy<SrcTrans, DstTrans, Document, Document>
            }

            if (r.contains("prepare")) {
                return request as TransactionPolicy<SrcTrans, DstTrans, Document, Document>
            }

            if (r.contains("finalize")) {
                return serialize as TransactionPolicy<SrcTrans, DstTrans, Document, Document>
            }

            if (r.contains("serialize")) {
                return export as TransactionPolicy<SrcTrans, DstTrans, Document, Document>
            }
        }

        throw IllegalArgumentException()
    }
}

class DocumentPolicyStorageFactoryCollector(private val dictionary: DocumentPolicyStorageFactory) {
    fun getFactory(type: DocumentType): DocumentPolicyStorageFactory {
        return dictionary
    }
}
