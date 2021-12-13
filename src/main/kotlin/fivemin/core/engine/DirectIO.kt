package fivemin.core.engine

enum class UsingPath{
    EXPORT, RESUME, TEMP
}

interface DirectIO {
    fun getToken(path : UsingPath) : DirectoryIOToken
}

class DirectIOImpl(val configController: ConfigController) : DirectIO {
    val pathDic : Map<UsingPath, String>

    init{
        val exp = configController.getSettings<String>("ExportPath").fold({"Output"}, {x -> x})
        val res = configController.getSettings<String>("ResumePath").fold({"Resume"}, {x -> x})
        var tmp = configController.getSettings<String>("TempPath").fold({"Temp"}, {x -> x})

        pathDic = mapOf(
            UsingPath.EXPORT to exp,
            UsingPath.RESUME to res,
            UsingPath.TEMP to tmp
        )
    }

    override fun getToken(path: UsingPath): DirectoryIOToken {
        return DirectoryIOToken(System.getProperty("user.dir")).withAdditionalPathDirectory(DirectoryIOToken(pathDic[path]!!))
    }

}