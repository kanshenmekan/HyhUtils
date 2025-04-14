package com.huyuhui.hyhutilskotlin.file

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.security.MessageDigest
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow


object FileUtils {
    const val TAG = "FileUtils"

    suspend fun copyDataFromUri(context: Context, sourceUri: Uri, destinationUri: Uri): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val resolver = context.contentResolver
                val input = resolver.openInputStream(sourceUri)
                    ?: throw FileNotFoundException("无法打开源文件: $sourceUri")
                val output = resolver.openOutputStream(destinationUri)
                    ?: throw FileNotFoundException("无法打开目标文件: $destinationUri")
                input.use { inputStream ->
                    output.use { outputStream ->
                        inputStream.copyTo(outputStream, bufferSize = 8 * 1024) // 8KB缓冲区
                        outputStream.flush()
                    }
                }
                true
            } catch (e: SecurityException) {
                Log.e(TAG, "安全异常，缺少文件访问权限", e)
                false
            } catch (e: FileNotFoundException) {
                Log.e(TAG, "文件未找到：${e.message}", e)
                false
            } catch (e: IOException) {
                Log.e(TAG, "IO异常：${e.message}", e)
                false
            } catch (e: Exception) {
                Log.e(TAG, "未知异常：${e.javaClass.simpleName}", e)
                false
            }
        }

    /**
     * Write file from string.
     *
     * @param file    The file.
     * @param content The string of content.
     * @param append  True to append, false otherwise.
     * @return `true`: success<br></br>`false`: fail
     */
    fun writeFileFromString(
        file: File?,
        content: String?,
        charset: Charset = Charsets.UTF_8,
        append: Boolean = false
    ): Boolean {
        if (file == null || content == null) return false
        if (!createOrExistsFile(file)) {
            Log.e("FileIOUtils", "create file <$file> failed.")
            return false
        }
        return try {
            if (append) {
                file.appendText(content, charset)
            } else {
                file.writeText(content, charset)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun writeFileFromStringAsync(
        file: File?,
        content: String?,
        charset: Charset = Charsets.UTF_8,
        append: Boolean = false
    ) =
        withContext(Dispatchers.IO) {
            return@withContext writeFileFromString(file, content, charset, append)
        }

    /**
     * Return the string in file.
     *
     * @param file        The file.
     * @param charsetName The name of charset.
     * @return the string in file
     */
    fun readFile2String(
        context: Context,
        file: File?,
        charsetName: Charset = Charsets.UTF_8
    ): String? {
        val bytes: ByteArray = readFile2BytesByStream(context, file) ?: return null
        return try {
            String(bytes, charsetName)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            null
        }

    }

    suspend fun readFile2StringAsync(
        context: Context,
        file: File?,
        charsetName: Charset = Charsets.UTF_8
    ) =
        withContext(Dispatchers.IO) {
            return@withContext readFile2String(context, file, charsetName)
        }

    fun readFile2BytesByStream(context: Context, file: File?): ByteArray? {
        return if (!isFileExists(context, file)) null else try {
            file!!.readBytes()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun readFile2BytesByStreamAsync(context: Context, file: File?) =
        withContext(Dispatchers.IO) {
            readFile2BytesByStream(context, file)
        }

    fun isExternalStorageMounted(): Boolean {
        return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
    }

    fun isFileExists(context: Context, file: File?): Boolean {
        if (file == null) return false
        return if (file.exists()) {
            true
        } else isFileExists(context, file.absolutePath)
    }

    fun isFileExists(context: Context, filePath: String?): Boolean {
        if (filePath == null) return false
        val file = File(filePath)
        return if (file.exists()) {
            true
        } else isFileExistsApi29(context, filePath)
    }

    private fun isFileExistsApi29(context: Context, filePath: String): Boolean {
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                val uri = filePath.toUri()
                val cr: ContentResolver = context.contentResolver
                val afd = cr.openAssetFileDescriptor(uri, "r") ?: return false
                try {
                    afd.close()
                } catch (ignore: IOException) {
                }
            } catch (e: FileNotFoundException) {
                return false
            }
            return true
        }
        return false
    }

    fun createOrExistsDir(dirPath: String?): Boolean {
        if (dirPath == null) return false
        return createOrExistsDir(File(dirPath))
    }

    fun createOrExistsDir(file: File?): Boolean {
        return file != null && if (file.exists()) file.isDirectory else file.mkdirs()
    }

    /**
     * Create a file if it doesn't exist, otherwise do nothing.
     *
     * @param filePath The path of file.
     * @return `true`: exists or creates successfully<br></br>`false`: otherwise
     */
    fun createOrExistsFile(filePath: String?): Boolean {
        if (filePath == null) return false
        return createOrExistsFile(File(filePath))
    }

    /**
     * Create a file if it doesn't exist, otherwise do nothing.
     *
     * @param file The file.
     * @return `true`: exists or creates successfully<br></br>`false`: otherwise
     */
    fun createOrExistsFile(file: File?): Boolean {
        if (file == null) return false
        if (file.exists()) return file.isFile
        return if (!createOrExistsDir(file.parentFile)) false else try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Create a file if it doesn't exist, otherwise delete old file before creating.
     *
     * @param filePath The path of file.
     * @return `true`: success<br></br>`false`: fail
     */
    fun createFileByDeleteOldFile(filePath: String?): Boolean {
        if (filePath == null) return false
        return createFileByDeleteOldFile(File(filePath))
    }

    /**
     * Create a file if it doesn't exist, otherwise delete old file before creating.
     *
     * @param file The file.
     * @return `true`: success<br></br>`false`: fail
     */
    fun createFileByDeleteOldFile(file: File?): Boolean {
        if (file == null) return false
        // file exists and unsuccessfully delete then return false
        if (file.exists() && !file.delete()) return false
        return if (!createOrExistsDir(file.parentFile)) false else try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Delete the file.
     *
     * @param file The file.
     * @return `true`: success<br></br>`false`: fail
     */
    fun deleteFile(file: File?): Boolean {
        return file != null && (!file.exists() || file.isFile && file.delete())
    }

    /**
     * Delete the directory.
     *
     * @param dir The directory.
     * @return `true`: success<br></br>`false`: fail
     */
    fun deleteDir(dir: File?): Boolean {
        if (dir == null) return false
        // dir doesn't exist then return true
        if (!dir.exists()) return true
        // dir isn't a directory then return false
        if (!dir.isDirectory) return false
        val files = dir.listFiles()
        if (files != null && files.isNotEmpty()) {
            for (file in files) {
                if (file.isFile) {
                    if (!file.delete()) return false
                } else if (file.isDirectory) {
                    if (!deleteDir(file)) return false
                }
            }
        }
        return dir.delete()
    }

    suspend fun deleteDirAsync(dir: File?) = withContext(Dispatchers.IO) {
        FileUtils.deleteDir(dir)
    }

    /**
     * Rename the file.
     *
     * @param file    The file.
     * @param newName The new name of file.
     * @return `true`: success<br></br>`false`: fail
     */
    fun rename(file: File?, newName: String?): Boolean {
        // file is null then return false
        if (file == null) return false
        // file doesn't exist then return false
        if (!file.exists()) return false
        // the new name is space then return false
        if (newName.isNullOrEmpty()) return false
        // the new name equals old name then return true
        if (newName == file.getName()) return true
        return file.parent?.let {
            val newFile = File(it + File.separator + newName)
            // the new name of file exists then return false
            return !newFile.exists()
                    && file.renameTo(newFile)
        } ?: false
    }

    /** 复制文件 */
    fun copy(sourcePath: String, destPath: String): Boolean {
        return try {
            FileInputStream(sourcePath).use { input ->
                FileOutputStream(destPath).use { output ->
                    input.copyTo(output)
                    true
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    fun getMD5FromFile(path: String): ByteArray? {
        val file = File(path)
        if (!file.exists() || !file.isFile()) {
            return null
        }
        val fis: InputStream?
        val buffer = ByteArray(1024 * 8)
        var numRead: Int
        val md5: MessageDigest
        try {
            fis = FileInputStream(path)
            md5 = MessageDigest.getInstance("MD5")
            fis.use { input ->
                while ((input.read(buffer).also { numRead = it }) != -1) {
                    md5.update(buffer, 0, numRead)
                }
            }
            return md5.digest()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }


    suspend fun getFileMD5Async(path: String): ByteArray? = withContext(Dispatchers.IO) {
        getMD5FromFile(path)
    }


    fun isUriFromFile(uri: Uri): Boolean {
        return uri.scheme == "file"
    }

    /** 获取文件扩展名 */
    fun getFileExtension(filePath: String): String? {
        return filePath.substringAfterLast('.', "").takeIf { it.isNotEmpty() }
    }

    /** 获取文件 MIME 类型 */
    fun getMimeType(filePath: String): String? {
        val extension = getFileExtension(filePath) ?: return null
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }


    /**
     * 格式化字节数为易读单位
     * @param sizeInBytes 字节数
     */
    fun formatSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) return "0 B"

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(sizeInBytes.toDouble()) / log10(1024.0)).toInt().coerceAtMost(4)
        val size = sizeInBytes / 1024.0.pow(digitGroups.toDouble())

        return when {
            digitGroups == 0 -> "$sizeInBytes B" // 直接显示字节
            size >= 100 -> DecimalFormat("#,##0").format(size) + " ${units[digitGroups]}"
            size >= 10 -> DecimalFormat("#0.0").format(size) + " ${units[digitGroups]}"
            else -> DecimalFormat("#0.00").format(size) + " ${units[digitGroups]}"
        }
    }

    /**
     * 获取文件或文件夹的格式化大小（自动转换单位）
     * @param path 文件或文件夹路径
     * @return 示例："2.5 MB" 或 "3.8 GB"
     */
    fun getFormattedSize(context: Context, path: String): String? {
        return try {
            val length = if (File(path).isDirectory) {
                getDirSize(path)
            } else {
                if (isFileExists(context, path)) {
                    File(path).length()
                } else -1
            }
            if (length == -1L) null
            formatSize(length)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    /** 获取目录大小（递归计算） */
    fun getDirSize(dirPath: String): Long {
        return if (createOrExistsDir(dirPath)) {
            val dir = File(dirPath)
            try {
                dir.walk().filter { it.isFile }.sumOf { it.length() }
            } catch (e: Exception) {
                e.printStackTrace()
                -1
            }
        } else -1
    }

    suspend fun getDirSizeAsync(dirPath: String) = withContext(Dispatchers.IO) {
        getDirSize(dirPath)
    }

}

