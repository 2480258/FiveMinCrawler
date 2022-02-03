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

import arrow.core.Option
import arrow.core.getOrElse

enum class UsingPath {
    EXPORT, RESUME, TEMP
}

interface DirectIO {
    fun getToken(path: UsingPath): DirectoryIOToken
}

class DirectIOImpl(val configController: ConfigController, val mainPath: Option<String>) : DirectIO {
    val pathDic: Map<UsingPath, String>
    val rootPath: String
    init {
        val exp = configController.getSettings("ExportPath").fold({ "Output" }, { x -> x })
        val res = configController.getSettings("ResumePath").fold({ "Resume" }, { x -> x })
        var tmp = configController.getSettings("TempPath").fold({ "Temp" }, { x -> x })

        pathDic = mapOf(
            UsingPath.EXPORT to exp,
            UsingPath.RESUME to res,
            UsingPath.TEMP to tmp
        )

        rootPath = mainPath.getOrElse { System.getProperty("user.dir") }
    }

    override fun getToken(path: UsingPath): DirectoryIOToken {
        return DirectoryIOToken(rootPath).withAdditionalPathDirectory(DirectoryIOToken(pathDic[path]!!))
    }
}
