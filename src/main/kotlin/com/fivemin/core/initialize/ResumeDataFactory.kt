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
import com.fivemin.core.LoggerController
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class ResumeDataFactory {

    companion object {
        private val logger = LoggerController.getLogger("ResumeDataFactory")
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun get(by: ByteArray): Either<Throwable, ResumeOption> {
        return Either.catch {
            val ret = ProtoBuf.decodeFromByteArray<ResumeOption>(by)

            ret
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun save(option: ResumeOption): ByteArray {
        logger.info("Serializing resume file")
        return ProtoBuf.encodeToByteArray(option)
    }
}

class ResumeDataNameGenerator(val option: StartTaskOption) {
    fun generate(): String {

        val sdf = SimpleDateFormat("ss")
        val cur = sdf.format(Date())

        return "[" + cur + "] " + URI(option.mainUriTarget).host + ".dat"
    }
}
