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

import arrow.core.*
import com.fivemin.core.LoggerController
import com.fivemin.core.logger.Log
import com.fivemin.core.logger.LogLevel
import kotlinx.serialization.Serializable

interface ExportState {
    fun export(): ContinueExportStateInfo

    fun create(token: ExportHandle): PreprocessedExport
}

@Serializable
data class ContinueExportStateInfo constructor(private val exportInfoSet: List<ExportInfo>) {
    val exports = exportInfoSet
}

class PreprocessedExportInfo constructor(val token: FileIOToken)

class PreprocessedExport constructor(val info: PreprocessedExportInfo, val data: ExportData) {
    
    @Log(
        beforeLogLevel = LogLevel.TRACE,
        afterReturningLogLevel = LogLevel.INFO,
        afterThrowingLogLevel = LogLevel.ERROR,
        afterReturningMessage = "Saved file",
        afterThrowingMessage = "Failed to export and save files"
    )
    fun save(): Either<Throwable, ExportResultToken> {
        val ret = Either.catch {
            if (data.isSaved) {
                IllegalStateException().left()
            }

            data.save(info.token)
        }.flatten()
        
        return ret
    }
}
