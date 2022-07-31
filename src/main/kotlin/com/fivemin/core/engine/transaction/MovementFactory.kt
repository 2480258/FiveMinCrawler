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

import com.fivemin.core.engine.Request
import com.fivemin.core.engine.StrictTransaction
import com.fivemin.core.engine.Transaction

interface MovementFactory<Document : Request> {
    fun <Document : Request> findRequest(): ExecuteRequestMovement<Document>

    fun <Document : Request> findExport(): ExecuteExportMovement<Document>

    fun <Document : Request> findPrepare(): PrepareRequestMovement<Document>

    fun <Document : Request> findSerialize(): ExecuteSerializeMovement<Document>
}


interface TransactionMovementFactory<in SrcTrans : Transaction<Document>, DstTrans : StrictTransaction<SrcTrans, Document>, Document : Request> {
    fun getMovement() : TransactionMovement<SrcTrans, DstTrans, Document>
}