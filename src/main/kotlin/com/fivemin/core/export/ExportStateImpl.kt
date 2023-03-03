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

package com.fivemin.core.export

import arrow.core.Option
import com.fivemin.core.engine.*
import com.fivemin.core.logger.Log
import com.fivemin.core.logger.LogLevel

/**
 * Manages current export file duplication state.
 */
class ExportStateImpl(private val directIO: DirectIO, private val continueExportStateInfo: Option<ContinueExportStateInfo>) : ExportState {
    private val set: MutableSet<ExportInfo>
    private val DUP_STRING: String = " - (Dup)"

    private val lock: Any = Any()

    private val directoryIOToken: DirectoryIOToken = directIO.getToken(UsingPath.EXPORT)

    init {
        set = continueExportStateInfo.fold({ mutableSetOf<ExportInfo>() }, { x -> x.exports.toMutableSet() })
    }

    override fun export(): ContinueExportStateInfo {
        return ContinueExportStateInfo(set.toList())
    }
    
    @Log(
        beforeLogLevel = LogLevel.TRACE,
        afterReturningLogLevel = LogLevel.TRACE,
        afterThrowingLogLevel = LogLevel.ERROR,
        afterReturningMessage = "creating export handle",
        afterThrowingMessage = "Failed to create export handle"
    )
    override fun create(token: ExportHandle): PreprocessedExport {
        return createInternal(token)
    }

    private fun createInternal(handle: ExportHandle): PreprocessedExport {
        synchronized(lock) {
            if (checkFilenameDuplicate(handle.request)) {
                return createInternal(handle.withNewExportInfo(handle.request.addSuffix(DUP_STRING)))
            }

            set.add(handle.request)

            return PreprocessedExport(getInfo(handle), handle.data)
        }
    }

    private fun checkFilenameDuplicate(info: ExportInfo): Boolean {
        return set.any {
            it.token.fileName == info.token.fileName
        }
    }

    private fun getInfo(handle: ExportHandle): PreprocessedExportInfo {
        return PreprocessedExportInfo(handle.request.token)
    }
}
