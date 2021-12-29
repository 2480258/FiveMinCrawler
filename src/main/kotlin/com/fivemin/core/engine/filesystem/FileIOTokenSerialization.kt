package com.fivemin.core.engine.filesystem

import com.fivemin.core.engine.FileIOToken
import com.fivemin.core.engine.DirectoryIOToken
import com.fivemin.core.engine.FileName

class FileIOTokenSerialization constructor(val InitPath : String, val FileName : String){
    fun Deserialize() : FileIOToken {
        return FileIOToken(DirectoryIOToken(InitPath), FileName(FileName))
    }
}