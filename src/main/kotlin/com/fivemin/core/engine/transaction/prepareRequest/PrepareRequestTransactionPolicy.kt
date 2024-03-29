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

package com.fivemin.core.engine.transaction.prepareRequest

import com.fivemin.core.engine.InitialTransaction
import com.fivemin.core.engine.PrepareTransaction
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.transaction.AbstractPolicy
import com.fivemin.core.engine.transaction.AbstractPolicyOption
import com.fivemin.core.engine.transaction.TransactionMovementFactory

class PrepareRequestTransactionPolicy<Document : Request>(
    option: AbstractPolicyOption<InitialTransaction<Document>, PrepareTransaction<Document>, Document>,
    movementFactory: TransactionMovementFactory<InitialTransaction<Document>, PrepareTransaction<Document>, Document>
) : AbstractPolicy<InitialTransaction<Document>, PrepareTransaction<Document>, Document>(option, movementFactory)