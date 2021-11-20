package core.engine.filesystem

import core.engine.FileIOToken
import core.engine.DirectoryIOToken
import core.engine.FileName

class FileIOTokenSerialization constructor(val InitPath : String, val FileName : String){
    fun Deserialize() : FileIOToken {
        return FileIOToken(DirectoryIOToken(InitPath), FileName(FileName))
    }
}