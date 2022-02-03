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

package com.fivemin.core.engine.transaction.export

import arrow.core.Either
import arrow.core.left
import com.fivemin.core.engine.ExportData
import com.fivemin.core.engine.ExportResultToken
import com.fivemin.core.engine.FileIOToken

class MemoryExportData(private val data: ByteArray) : ExportData {
    override var isSaved: Boolean = false

    override fun save(token: FileIOToken): Either<Throwable, ExportResultToken> {

        return Either.catch {
            if (isSaved) {
                IllegalArgumentException().left()
            }

            token.openFileWriteStream {
                it.write(data)
                isSaved = true
            }

            ExportResultToken(token)
        }
    }
}
