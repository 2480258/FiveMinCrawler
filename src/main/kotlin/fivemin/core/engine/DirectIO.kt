package fivemin.core.engine

import arrow.core.Option
import arrow.core.getOrElse

enum class UsingPath{
    EXPORT, RESUME, TEMP
}

interface DirectIO {
    fun getToken(path : UsingPath) : DirectoryIOToken
}

class DirectIOImpl(val configController: ConfigController, val mainPath : Option<String>) : DirectIO {
    val pathDic : Map<UsingPath, String>
    val rootPath : String
    init{
        val exp = configController.getSettings<String>("ExportPath").fold({"Output"}, {x -> x})
        val res = configController.getSettings<String>("ResumePath").fold({"Resume"}, {x -> x})
        var tmp = configController.getSettings<String>("TempPath").fold({"Temp"}, {x -> x})

        pathDic = mapOf(
            UsingPath.EXPORT to exp,
            UsingPath.RESUME to res,
            UsingPath.TEMP to tmp
        )

        rootPath = mainPath.getOrElse { System.getProperty("user.dir") }
    }

    override fun getToken(path: UsingPath): DirectoryIOToken {
        return DirectoryIOToken(rootPath).withAdditionalPathDirectory(DirectoryIOToken(pathDic[path]!!))
    }

}