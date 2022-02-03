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

import com.fivemin.core.engine.*
import com.fivemin.core.export.ExportInfoImpl
import java.io.InputStream

data class ExportHandleImpl(override val request: ExportInfo, override val data: ExportData) : ExportHandle {
    override fun withNewExportInfo(info: ExportInfo): ExportHandle {
        return ExportHandleImpl(info, data)
    }
}

class ExportHandleFactoryImpl(private val io: DirectIO, private val bookName: String) : ExportHandleFactory {
    private val mainPath: DirectoryIOToken

    init {
        mainPath = io.getToken(UsingPath.EXPORT)
    }

    override fun create(additionalPath: String, ret: InputStream): ExportHandle {
        val info = ExportInfoImpl(mainPath.withAdditionalPathFile(additionalPath))
        val data = StreamExportData(ret)

        return ExportHandleImpl(info, data)
    }

    override fun create(additionalPath: String, ret: String): ExportHandle {
        val info = ExportInfoImpl(mainPath.withAdditionalPathFile(additionalPath))
        val array = ret.toByteArray(Charsets.UTF_8)

        val data = MemoryExportData(array)

        return ExportHandleImpl(info, data)
    }

    override fun create(additionalPath: String, token: FileIOToken): ExportHandle {
        val info = ExportInfoImpl(mainPath.withAdditionalPathFile(additionalPath))
        val data = FileInfoExportData(token)

        return ExportHandleImpl(info, data)
    }
}
