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

package com.fivemin.core.engine.transaction.serialize

import com.fivemin.core.engine.FinalizeRequestTransaction
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.SerializeTransaction
import com.fivemin.core.engine.transaction.AbstractPolicy
import com.fivemin.core.engine.transaction.AbstractPolicyOption
import com.fivemin.core.engine.transaction.TransactionMovementFactory

class SerializeTransactionPolicy<Document : Request>(
    private val option: AbstractPolicyOption<FinalizeRequestTransaction<Document>, SerializeTransaction<Document>, Document>,
    private val movementFactory: TransactionMovementFactory<FinalizeRequestTransaction<Document>, SerializeTransaction<Document>, Document>
) : AbstractPolicy<FinalizeRequestTransaction<Document>, SerializeTransaction<Document>, Document>(
    option, movementFactory
)
