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
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.ExportData
import com.fivemin.core.engine.ExportResultToken
import com.fivemin.core.engine.FileIOToken
import com.fivemin.core.logger.Log
import com.fivemin.core.logger.LogLevel
import java.io.InputStream

class StreamExportData(private val data: InputStream) : ExportData {
    
    
    override var isSaved: Boolean = false
    
    @Log(
        beforeLogLevel = LogLevel.DEBUG,
        afterReturningLogLevel = LogLevel.DEBUG,
        afterThrowingLogLevel = LogLevel.ERROR,
        beforeMessage = "coping memory to filesystem (streamexportdata)",
        afterThrowingMessage = "failed to copy memory to filesystem (streamexportdata)"
    )
    override fun save(token: FileIOToken): Either<Throwable, ExportResultToken> {
        val ret = Either.catch {
            data.use { data ->
                if (isSaved || token.fileExists()) {
                    IllegalArgumentException().left()
                }

                token.openFileWriteStream {
                    data.copyTo(it)
                    isSaved = true
                }
            }

            ExportResultToken(token)
        }
        
        return ret
    }
}
