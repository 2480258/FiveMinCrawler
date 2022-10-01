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

package com.fivemin.core.engine

import arrow.core.Either
import arrow.core.Valid
import com.fivemin.core.LoggerController
import kotlinx.serialization.Serializable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * Represent Directory path.
 */
class DirectoryIOToken constructor(private val additionalPath: String) {
    val Path: File
    
    /**
     * Create DirectoryIOToken
     * All forbidden chars and names(NTFS) are filtered.
     *
     * Throws if file path is given. Use FileIOToken instead.
     */
    init {
        val f = Paths.get(File(additionalPath).path)
        var path = additionalPath

        //absolute path always rooted
        if (f.isAbsolute) {
            val root = f.root //root should be preserved because of C:\\ ( ':' is forbidden for directory chars)
            val left = additionalPath.subSequence(root.toString().length until additionalPath.length).toString()
            
            path = Paths.get(root.toString(), checkPathNameOSSelective(left)).toString()
        }

        Path = File(path)

        if (Path.isFile) {
            throw InvalidPathException(path, "is file")
        }
    }
    
    /**
     * Add directory to path
     * C:\abc\ => C:\abc\additional_directory
     */
    fun withAdditionalPathDirectory(additional: DirectoryIOToken): DirectoryIOToken {
        if (additional.Path.isFile || additional.Path.isAbsolute || additional.Path.isRooted) {
            throw IllegalArgumentException()
        }

        return DirectoryIOToken(appendPath(Path.absolutePath, additional.additionalPath))
    }
    
    /**
     * Add file name to path
     * C:\abc\ => C:\abc\file.txt
     */
    fun withAdditionalPathFile(additional: String): FileIOToken {
        val f = File(Path, additional)

        if (f.isDirectory) {
            throw IllegalArgumentException()
        }

        val file = FileName(f.name)

        if (f.parent.isNotEmpty()) {
            val dir = DirectoryIOToken(f.parent)
            return FileIOToken(dir, file)
        }

        return FileIOToken(this, file)
    }

    companion object {
        val ErrorChar: Char

        init {
            ErrorChar = '_'
        }
        
        private fun checkPathNameOSSelective(name: String): String {
            val curOs = System.getProperty("os.name")
            
            if(curOs.lowercase().contains("nix")) {
                return checkPathNameLinux(name)
            }
            else if(curOs.lowercase().contains("windows")) {
                return checkPathNameWindows(name)
            }
            else {
                return checkPathNameLinux(name)
            }
        }

        private fun checkPathNameWindows(name: String): String {
            return name.split('"', '<', '>', '|', ':', '*', '?', '/').reduce { x, y -> x + ErrorChar + y }
        }
    
        private fun checkPathNameLinux(name: String): String {
            return name.split('"', '<', '>', '|', ':', '*', '?', '\\').reduce { x, y -> x + ErrorChar + y }
        }
    
    
        private fun appendPath(path1: String, path2: String): String {
            val sep1 = path1.last() == File.separatorChar
            val sep2 = path2.last() == File.separatorChar

            return if (sep1 && sep2) {
                path1.subSequence(0, path1.length - 1).toString() + path2
            } else if (sep1 || sep2) {
                path1 + path2
            } else {
                path1 + System.getProperty("file.separator") + path2
            }
        }
    }
}
@Serializable
data class FileName constructor(private val filename: String) {
    override fun equals(other: Any?): Boolean {
        if (other != null && other is FileName) {
            return name.name == other.name.name
        }

        return false
    }

    override fun hashCode(): Int {
        return filename.hashCode()
    }

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

/**
 * Represent File
 *
 * @param InitPath Absolute path DirectoryIOToken
 * @param name File name
 */
data class FileIOToken constructor(private val InitPath: DirectoryIOToken, private val name: FileName) {
    
    companion object {
        private val logger = LoggerController.getLogger("FileIOToken")
    }
    
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

    fun fileExists(): Boolean {
        return result.exists()
    }
    
    /**
     * Add suffix name for file. extensions are considered.
     * abc.txt => abc - (Dup).txt
     */
    fun addSuffix(suffix: String): FileIOToken {
        return withDifferentName(fileName.name.nameWithoutExtension + suffix)
    }

    private fun withDifferentName(nameWithoutExt: String): FileIOToken {
        var replace = nameWithoutExt + "." + fileName.name.extension
        return FileIOToken(directoryPart, FileName(replace))
    }

    private fun ensureDirectory() {
        if (!Files.exists(directoryPart.Path.toPath())) {
            Files.createDirectories(directoryPart.Path.toPath())
        }
    }
    
    /**
     * Opens file stream which users should manually close them.
     */
    fun unsafeOpenFileStream(): Either<Throwable, FileOutputStream> {
        ensureDirectory()

        val ret = Either.catch {
            return Either.Right(FileOutputStream(result))
        }
        
        logger.debug(ret, "failed to unsafeOpenFileStream")
        
        return ret
    }
    
    /**
     * Opens file write stream. closes itself if lambda finished.
     */
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
    
    /**
     * Opens file read stream. closes itself if lambda finished.
     */
    fun <T> openFileReadStream(func: (FileInputStream) -> T): Either<Throwable, T> {
        ensureDirectory()
        val ret = Either.catch { ->
            var os: FileInputStream? = null

            try {
                os = FileInputStream(result)
                return Either.Right(func(os))
            } finally {
                os?.close()
            }
        }
        
        logger.debug(ret, "openFileReadStream")
        
        return ret
    }

    fun remove() {
        Files.delete(result.toPath())
    }
    
    /**
     * Moves file to here from source (Dest is this file path).
     *
     * @param source Files which want to move.
     */
    fun moveFileToPath(source: FileIOToken) {
        ensureDirectory()

        Files.move(source.result.toPath(), result.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
}
