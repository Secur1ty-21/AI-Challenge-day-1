package ru.yamost.first.agent.featute.chat.data.storage

import android.content.Context
import java.io.*
import java.util.UUID

object Installation {
    private var sID: String? = null
    private const val INSTALLATION = "INSTALLATION"

    @Synchronized
    fun id(appDir: File): String {
        var id = sID
        if (id == null) {
            val installation = File(appDir, INSTALLATION)
            try {
                if (!installation.exists()) writeInstallationFile(installation)
                id = readInstallationFile(installation)
                sID = id
            } catch (e: Exception) {
                e.printStackTrace()
                return UUID.randomUUID().toString()
            }
        }
        return id
    }

    @Throws(IOException::class)
    private fun writeInstallationFile(installation: File) {
        val fOut = FileOutputStream(installation)
        val id = UUID.randomUUID().toString()
        fOut.write(id.toByteArray())
        fOut.close()
    }

    @Throws(IOException::class)
    private fun readInstallationFile(installation: File): String {
        val fIn = RandomAccessFile(installation, "r")
        val bytes = ByteArray(fIn.length().toInt())
        fIn.readFully(bytes)
        fIn.close()
        return String(bytes)
    }
}
