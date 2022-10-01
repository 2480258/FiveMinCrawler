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

package com.fivemin.core.initialize

import arrow.core.Either
import arrow.core.Option
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.FileIOToken
import com.fivemin.core.engine.session.BloomFilterFactory
import com.fivemin.core.engine.session.SerializedBloomFilterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.File
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class ResumeDataNameGenerator(val target: String) {
    fun generate(resumeAt: Option<String>): String {
        return resumeAt.fold({
            val sdf = SimpleDateFormat("ss")
            val cur = sdf.format(Date())
    
            "jdbc:sqlite:[ + $cur + ]_ + ${URI(target).host} + .db"
        }, {
            "jdbc:sqlite:$it"
        })
    }
}
