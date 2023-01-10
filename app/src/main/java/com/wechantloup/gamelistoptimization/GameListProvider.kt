package com.wechantloup.gamelistoptimization

import android.util.Log
import com.google.gson.Gson
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import com.wechantloup.gamelistoptimization.model.GameList
import com.wechantloup.gamelistoptimization.model.GameListHolder
import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.model.Source
import fr.arnaudguyon.xmltojsonlib.JsonToXml
import fr.arnaudguyon.xmltojsonlib.XmlToJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.EnumSet

class GameListProvider {

    private val gson = Gson()
    private var share: DiskShare? = null
    private var currentSource: Source? = null

    suspend fun open(source: Source): Boolean = withContext(Dispatchers.IO) {
        if (source != currentSource) {
            if (share?.isConnected == true) {
                share?.close()
            }
            currentSource = source
            share = source.connectTo()
        }
        return@withContext share != null
    }

    suspend fun close() = withContext(Dispatchers.IO) {
        currentSource = null
        share?.close()
        share = null
    }

    suspend fun getPlatforms(): List<Platform> = withContext(Dispatchers.IO) {
        val platforms = mutableListOf<Platform>()
        val share = share ?: throw IllegalStateException("Not connected")

        for (file in share.listClean("", "*")) {
            val folderName = file.fileName
            if (share.isFolder("", folderName)) {
                val filePath = "$folderName\\${GAMELIST_FILE}"
                share.extractGameList(folderName, GAMELIST_FILE)?.let { it ->
                    val gameListBackup = share.extractGameList(folderName, GAMELIST_BACKUP_FILE)
                    platforms.add(Platform(it, gameListBackup, filePath))
                }
            }
        }
        platforms.sortedBy { it.toString() }
    }

    suspend fun savePlatform(platform: Platform) = withContext(Dispatchers.IO) {
        val share = share ?: throw IllegalStateException("Not connected")
        val holder = GameListHolder(platform.gameList)
        val path = platform.path
        val newJson = gson.toJson(holder)

        val jsonToXml = JsonToXml.Builder(newJson)
            .forceAttribute("/gameList/game/id")
            .forceAttribute("/gameList/game/source")
            .forceAttribute("/gameList/platform")
            .build()
        val newXml = jsonToXml.toFormattedString(2)

        Log.i("TOTO", "New xml = $newXml")

        val outFile = share.openFile(
            path,
            EnumSet.of(AccessMask.GENERIC_WRITE),
            null,
            SMB2ShareAccess.ALL,
            SMB2CreateDisposition.FILE_OVERWRITE,
            null)
        val outputStream = outFile.outputStream
        outputStream.use {
            it.write(newXml.toByteArray(Charsets.UTF_8))
        }
    }

    private fun DiskShare.extractGameList(folderName: String, fileName: String): GameList? {
        val filePath = "$folderName\\$fileName"

        if (!fileExists(filePath)) return null

        Log.i(TAG, "Reading $filePath")

        try {
            val readFile = openFile(
                filePath,
                EnumSet.of(AccessMask.GENERIC_READ),
                null,
                SMB2ShareAccess.ALL,
                SMB2CreateDisposition.FILE_OPEN,
                null
            )

            val inputStream = readFile.inputStream
            val xmlToJson: XmlToJson = XmlToJson.Builder(inputStream, null).build()
            inputStream.close()

            val jsonString = xmlToJson.toString()

            val holder = gson.fromJson(jsonString, GameListHolder::class.java)
            return holder.gameList
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing $filePath", e)
            return null
        }
    }

    private suspend fun Source.connectTo(): DiskShare? = withContext(Dispatchers.IO) {
        var connection: Connection? = null
        var session: Session? = null
        try {
            val client = SMBClient()
            connection = client.connect(ip)
            val ac = AuthenticationContext(login, password.toCharArray(), "DOMAIN")
            session = connection.authenticate(ac)
            (session.connectShare(path) as DiskShare)
        } catch (e: Exception) {
            Log.e(TAG, "Can't connect to source", e)
            session?.close()
            connection?.close()
            null
        }
    }

    private fun DiskShare.isFolder(path: String, fileName: String): Boolean =
        folderExists("$path\\$fileName")

    private fun DiskShare.listClean(path: String, pattern: String) =
        list(path, pattern).filter { it.fileName != "." && it.fileName != ".." }

    companion object {
        private const val TAG = "GameListProvider"
        private const val GAMELIST_FILE = "gamelist.xml"
        private const val GAMELIST_BACKUP_FILE = "gamelist.backup.xml"
    }
}
