package fivemin.core.initialize

import arrow.core.Either
import fivemin.core.engine.FileIOToken
import java.io.File
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class ResumeDataFactory {
    fun get(path : String) : Either<Throwable, ResumeOption> {
        return Either.catch {

            val f = File(path)

            val by = f.readBytes()

            val ret = ProtoBuf.decodeFromByteArray<ResumeOption>(by)

            ret
        }
    }

    fun save(token : FileIOToken, option : ResumeOption){
        token.openFileWriteStream {
            //TODO Log
            it.write(ProtoBuf.encodeToByteArray(option))
        }
    }
}

class ResumeDataNameGenerator(val option : StartTaskOption) {
    fun generate() : String{

        val sdf = SimpleDateFormat("ss")
        val cur = sdf.format(Date())

        return "[" + cur + "] " + URI(option.mainUriTarget).host + ".dat"
    }
}