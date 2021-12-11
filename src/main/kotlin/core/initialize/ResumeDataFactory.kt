package core.initialize

import arrow.core.Validated
import core.engine.FileIOToken
import java.io.File
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

class ResumeDataFactory {
    fun get(path : String) : Validated<Throwable, ResumeOption> {
        return Validated.catch {

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