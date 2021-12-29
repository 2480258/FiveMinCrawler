package com.fivemin.core.initialize

import arrow.core.Either
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.FileIOToken
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File
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
    fun get(by : ByteArray) : Either<Throwable, ResumeOption> {
        return Either.catch {
            val ret = ProtoBuf.decodeFromByteArray<ResumeOption>(by)

            ret
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun save(option : ResumeOption) : ByteArray{
        logger.info("Serializing resume file")
        return ProtoBuf.encodeToByteArray(option)
    }
}

class ResumeDataNameGenerator(val option : StartTaskOption) {
    fun generate() : String{

        val sdf = SimpleDateFormat("ss")
        val cur = sdf.format(Date())

        return "[" + cur + "] " + URI(option.mainUriTarget).host + ".dat"
    }
}