package fivemin.core.engine

import arrow.core.Either
import arrow.core.Valid
import arrow.core.foldRight
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.StandardCopyOption


class DirectoryIOToken constructor(private val additionalPath: String) {
    val Path: File

    init {
        var f = File(additionalPath)
        var path = additionalPath

        if (f.isRooted) {

            var drive = additionalPath.subSequence(0 until 3).toString()
            var left = additionalPath.subSequence(3 until additionalPath.length).toString()

            path = drive + checkPathName(left)
        }

        Path = File(path)

        if (Path.isFile) {
            throw InvalidPathException(path, "is file")
        }
    }

    fun withAdditionalPathDirectory(additional: DirectoryIOToken): DirectoryIOToken {
        if (additional.Path.isFile || additional.Path.isAbsolute || additional.Path.isRooted) {
            throw IllegalArgumentException()
        }

        return DirectoryIOToken(appendPath(Path.absolutePath, additional.additionalPath))
    }

    fun withAdditionalPathFile(additional: String): FileIOToken {
        var f = File(Path, additional)

        if (f.isDirectory) {
            throw IllegalArgumentException()
        }

        var file = FileName(f.name)

        if (f.parent.isNotEmpty()) {
            var dir = DirectoryIOToken(f.parent)
            return FileIOToken(dir, file)
        }

        return FileIOToken(this, file)
    }

    companion object {
        val ErrorChar: Char

        init {
            ErrorChar = '_'
        }

        private fun checkPathName(name: String): String {
            return name.split('"', '<', '>', '|', ':', '*', '?', '/').reduce { x, y -> x + ErrorChar + y }
        }

        private fun appendPath(path1: String, path2: String): String {
            val sep1 = path1.last() == File.separatorChar
            val sep2 = path2.last() == File.separatorChar

            return if (sep1 && sep2) {
                path1.subSequence(0, path1.length - 1).toString() + path2
            } else if (sep1 || sep2) {
                path1 + path2
            } else {
                path1 + '\\' + path2
            }
        }
    }

}
@Serializable
data class FileName constructor(private val filename: String) {
    val name: File
    get() = File(checkFileName(filename))

    init {

        if (name.nameWithoutExtension.isEmpty() || isPath(filename)) {
            throw IllegalArgumentException()
        }
    }

    companion object {
        val invalidFileNames: List<String>
        val ErrorChar: Char

        init {
            invalidFileNames = listOf(
                "CON",
                "PRN",
                "AUX",
                "NUL",
                "COM1",
                "COM2",
                "COM3",
                "COM4",
                "COM5",
                "COM6",
                "COM7",
                "COM8",
                "COM9",
                "LPT1",
                "LPT2",
                "LPT3",
                "LPT4",
                "LPT5",
                "LPT6",
                "LPT7",
                "LPT8",
                "LPT9"
            )
            ErrorChar = '_'
        }

        fun isPath(name: String): Boolean {
            return name.contains(File.separator)
        }


        fun checkFileName(name: String): String {
            if (invalidFileNames.contains(name.uppercase())) {
                return name + ErrorChar
            }

            return name.splitToSequence('"', '<', '>', '|', ':', '*', '?', '\\', '/').reduce({ x, y -> x + ErrorChar + y })
        }
    }
}

data class FileIOToken constructor(private val InitPath: DirectoryIOToken, private val name: FileName) {
    private val directoryPart: DirectoryIOToken
    val fileName: FileName

    private val result: File

    init {
        if (!InitPath.Path.isAbsolute) {
            throw IllegalArgumentException()
        }

        directoryPart = InitPath
        fileName = name

        result = File(directoryPart.Path, fileName.name.name)
    }

    fun exists(): Boolean {
        return result.exists()
    }

    fun addSuffix(suffix: String): FileIOToken {
        return withDifferentName(fileName.name.nameWithoutExtension + suffix)
    }

    private fun withDifferentName(nameWithoutExt: String): FileIOToken {
        var replace = nameWithoutExt + fileName.name.extension
        return FileIOToken(directoryPart, FileName(replace))
    }

    private fun ensureDirectory() {
        var p = Files.createDirectories(directoryPart.Path.toPath())
    }

    fun unsafeOpenFileStream(): Either<Throwable, FileOutputStream> {
        ensureDirectory()

        return Either.catch {
            return Either.Right(FileOutputStream(result))
        }
    }

    fun openFileWriteStream(func: (FileOutputStream) -> Unit) {
        ensureDirectory()
        var os: FileOutputStream? = null
        try {
            os = FileOutputStream(result)
            Valid(func(os))
        } finally {
            os?.close()
        }
    }

    fun <T> openFileReadStream(func: (FileInputStream) -> T): Either<Throwable, T> {
        ensureDirectory()
        return Either.catch { ->
            var os: FileInputStream? = null

            try {
                os = FileInputStream(result)
                return Either.Right(func(os))
            } finally {
                os?.close()
            }
        }
    }

    fun moveFileToPath(token: FileIOToken) {
        ensureDirectory()

        Files.move(token.result.toPath(), result.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
}