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

class BinaryExportAdapter(private val fileName: TagExpression, private val factory: ExportHandleFactory) :
    ExportAdapter {
    override fun parse(
        request: Request,
        info: Iterable<ExportAttributeInfo>
    ): Iterable<Either<Throwable, ExportHandle>> {
        val ret = info.map<ExportAttributeInfo, Either<Throwable, ExportHandle>> { x ->
            x.element.match<Either<Throwable, ExportHandle>>({ Either.Left(IllegalArgumentException()) }, { y ->
                y.successInfo.body.ifFile<Either<Throwable, ExportHandle>>({ z ->
                    Either.Right(factory.create(fileName.build(x.tagRepo), z.file))
                }, { z ->

                    z.openStreamAsByteAndDispose {
                        factory.create(fileName.build(x.tagRepo), it)
                    }
                })
            })
        }

        return ret
    }
}
