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
import com.fivemin.core.engine.FileIOToken
import com.fivemin.core.engine.Request
import java.io.InputStream

interface ExportAdapter {
    fun parse(request: Request, info: Iterable<ExportAttributeInfo>): Iterable<Either<Throwable, ExportHandle>>
}

interface ExportHandleFactory {
    fun create(additionalPath: String, ret: InputStream): ExportHandle

    fun create(additionalPath: String, ret: String): ExportHandle

    fun create(additionalPath: String, token: FileIOToken): ExportHandle
}
