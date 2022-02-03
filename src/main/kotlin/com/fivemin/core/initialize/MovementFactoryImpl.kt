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

package com.fivemin.core.initialize

import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.*
import com.fivemin.core.engine.transaction.export.ExportParser
import com.fivemin.core.engine.transaction.export.ExportTransactionMovement
import com.fivemin.core.engine.transaction.finalizeRequest.FinalizeRequestTransactionMovement
import com.fivemin.core.engine.transaction.finalizeRequest.RequestWaiter
import com.fivemin.core.engine.transaction.prepareRequest.PreParser
import com.fivemin.core.engine.transaction.prepareRequest.PrepareRequestTransactionMovement
import com.fivemin.core.engine.transaction.serialize.PostParser
import com.fivemin.core.engine.transaction.serialize.SerializeTransactionMovementImpl

class MovementFactoryImpl(private val pp: PreParser, private val rw: RequestWaiter, private val ep: ExportParser, private val es: ExportState, private val po: PostParser<Request>) : MovementFactory<Request> {
    override fun <Document : Request> findRequest(): ExecuteRequestMovement<Document> {
        return FinalizeRequestTransactionMovement(rw)
    }

    override fun <Document : Request> findExport(): ExecuteExportMovement<Document> {
        return ExportTransactionMovement(ep, es)
    }

    override fun <Document : Request> findPrepare(): PrepareRequestMovement<Document> {
        return PrepareRequestTransactionMovement(pp)
    }

    override fun <Document : Request> findSerialize(): ExecuteSerializeMovement<Document> {
        return SerializeTransactionMovementImpl(po)
    }
}
