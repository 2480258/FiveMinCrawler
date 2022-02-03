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

package com.fivemin.core.engine.transaction.finalizeRequest

import com.fivemin.core.engine.FinalizeRequestTransaction
import com.fivemin.core.engine.PrepareTransaction
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.transaction.*

class FinalizeRequestTransactionPolicy<Document : Request>(
    option: AbstractPolicyOption<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document>,
    movementFactory: MovementFactory<Document>
) : AbstractPolicy<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document>(
    option,
    movementFactory
) {
    override fun getMovement(factory: MovementFactory<Document>): TransactionMovement<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document> {
        return factory.findRequest()
    }
}
