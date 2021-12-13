package fivemin.core.engine.filesystem

import fivemin.core.engine.FileIOToken
import fivemin.core.engine.DirectoryIOToken
import fivemin.core.engine.FileName

class FileIOTokenSerialization constructor(val InitPath : String, val FileName : String){
    fun Deserialize() : FileIOToken {
        return FileIOToken(DirectoryIOToken(InitPath), FileName(FileName))
    }
}