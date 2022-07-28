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
import com.fivemin.core.initialize.DocumentTransaction
import kotlin.reflect.*


/**
 * Stores TransactionPolicies.
 */
class DocumentTypePolicyStorage<Document : Request>(
    val policies: Map<DocumentTransaction, Any>
) {
    /**
     * Returns TransactionPolicy for consumers.
     * Note that we should change this function if Transaction is added.
     */
    inline fun <reified SrcTrans : Transaction<Document>, reified DstTrans : StrictTransaction<SrcTrans, Document>> find(): TransactionPolicy<SrcTrans, DstTrans, Document, Document> {
        var r = SrcTrans::class.qualifiedName?.lowercase()
        
        if (r != null) {
            if (r.contains("init")) {
                return policies[DocumentTransaction.Prepare] as TransactionPolicy<SrcTrans, DstTrans, Document, Document>
            }

            if (r.contains("prepare")) {
                return policies[DocumentTransaction.Request] as TransactionPolicy<SrcTrans, DstTrans, Document, Document>
            }

            if (r.contains("finalize")) {
                return policies[DocumentTransaction.Serialize] as TransactionPolicy<SrcTrans, DstTrans, Document, Document>
            }

            if (r.contains("serialize")) {
                return policies[DocumentTransaction.Export] as TransactionPolicy<SrcTrans, DstTrans, Document, Document>
            }
        }

        throw IllegalArgumentException()
    }
}

class DocumentPolicyStorageFactoryCollector<Document : Request>(
    val policies: Map<DocumentTransaction, Any>
) {
    /**
     * Returns TransactionPolicy for consumers.
     * Note that we should change this function if Transaction is added.
     */
    inline fun <reified SrcTrans : Transaction<Document>, reified DstTrans : StrictTransaction<SrcTrans, Document>> find(): TransactionPolicy<SrcTrans, DstTrans, Document, Document> {
        var r = SrcTrans::class.qualifiedName?.lowercase()
        
        if (r != null) {
            if (r.contains("init")) {
                return policies[DocumentTransaction.Prepare] as TransactionPolicy<SrcTrans, DstTrans, Document, Document>
            }
            
            if (r.contains("prepare")) {
                return policies[DocumentTransaction.Request] as TransactionPolicy<SrcTrans, DstTrans, Document, Document>
            }
            
            if (r.contains("finalize")) {
                return policies[DocumentTransaction.Serialize] as TransactionPolicy<SrcTrans, DstTrans, Document, Document>
            }
            
            if (r.contains("serialize")) {
                return policies[DocumentTransaction.Export] as TransactionPolicy<SrcTrans, DstTrans, Document, Document>
            }
        }
        
        throw IllegalArgumentException()
    }
}