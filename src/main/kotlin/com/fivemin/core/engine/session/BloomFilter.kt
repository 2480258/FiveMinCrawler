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

package com.fivemin.core.engine.session

import arrow.core.Either
import com.fivemin.core.engine.SerializableAMQ
import com.fivemin.core.engine.UniqueKey
import java.io.InputStream
import java.io.OutputStream



interface BloomFilterFactory {
    fun createEmpty() : SerializableAMQ
    
}

interface SerializedBloomFilterFactory {
    fun createWithInput(inputStream: InputStream) : SerializableAMQ
}

//TODO: ISP에 따라 Save(io: FileIOToken)을 별도의 인터페이스로 하고, Name결정 방식과 분리할 것