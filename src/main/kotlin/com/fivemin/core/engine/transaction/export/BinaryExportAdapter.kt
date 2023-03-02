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
import com.fivemin.core.engine.ExportHandle
import com.fivemin.core.engine.Request
import com.fivemin.core.engine.ifFile
import com.fivemin.core.engine.match
import com.fivemin.core.logger.Log
import com.fivemin.core.logger.LogLevel

/**
 * Export adapter for binary files
 *
 * @param fileName A tag expression for file name.
 * @param factory factory method class for creating export handle.
 */
class BinaryExportAdapter(private val fileName: TagExpression, private val factory: ExportHandleFactory) :
    ExportAdapter {
    
    /**
     * parses and save to binary file
     *
     * @param info lists of file for saving.
     */
    
    
    override fun parseAndExport(
        request: Request, info: Iterable<ExportAttributeInfo>
    ): Iterable<Either<Throwable, ExportHandle>> {
        val results = info.map { x ->
            save(x)
        }
        
        return results
    }
    
    @Log(
        beforeLogLevel = LogLevel.DEBUG,
        afterReturningLogLevel = LogLevel.DEBUG,
        afterThrowingLogLevel = LogLevel.ERROR,
        beforeMessage = "exporting binary files",
        afterThrowingMessage = "failed to export binary files"
    )
    private fun save(x: ExportAttributeInfo) : Either<Throwable, ExportHandle> {
        return x.element.match({ Either.Left(IllegalArgumentException("tried to export text file with binary adapter: ${x.locator.info.name}")) },
            { y -> //filters internal attributes. no binary export for those.
                y.successInfo.body.ifFile({ z -> //if attribute is file, just move them.
                    Either.Right(factory.create(fileName.build(x.tagRepo), z.file))
                }, { z ->
                    
                    z.openStreamAsByteAndDispose { //if attribute is in memory, write to file.
                        factory.create(fileName.build(x.tagRepo), it)
                    }
                })
            })
    }
}
