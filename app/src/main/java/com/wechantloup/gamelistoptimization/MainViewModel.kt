package com.wechantloup.gamelistoptimization

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import fr.arnaudguyon.xmltojsonlib.JsonToXml
import fr.arnaudguyon.xmltojsonlib.XmlToJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.EnumSet

class MainViewModelFactory(private val activity: Activity) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(activity.application) as T
    }
}

class MainViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _stateFlow = MutableStateFlow(State())
    val stateFlow: StateFlow<State> = _stateFlow

    private val gson = Gson()

    private val gameSources = listOf(
        Source.NAS,
        Source.RETROPIE
    )

    private var share: DiskShare? = null
    /*@Suppress("BlockingMethodInNonBlockingContext")
    private val share: Deferred<DiskShare?> = viewModelScope.async(Dispatchers.IO) {
        var connection: Connection? = null
        var session: Session? = null
        try {
            val client = SMBClient()

            connection = client.connect(NAS_IP)
            val ac = AuthenticationContext(NAS_LOGIN, NAS_PWD.toCharArray(), "DOMAIN")
            session = connection.authenticate(ac)
            (session.connectShare(NAS_ROOT) as DiskShare)
        } catch (e: Exception) {
            // ToDo
            session?.close()
            connection?.close()
            null
        }
    }*/

    init {
        _stateFlow.value = stateFlow.value.copy(sources = gameSources)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override fun onCleared() {
        viewModelScope.launch(Dispatchers.IO) {
            share?.close()
            super.onCleared()
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun setSource(source: Source) {
        viewModelScope.launch(Dispatchers.IO) {
            if (share?.isConnected == true) {
                share?.close()
            }
            share = source.connectTo()
            _stateFlow.value = stateFlow.value.copy(platforms = getPlatforms())
        }
    }

    fun onGameSetForKids(platform: Platform, gameId: String, value: Boolean) {
        platform.gameList.games.first { it.id == gameId }.kidgame = value
        viewModelScope.launch { savePlatform(platform) }
    }

    fun onGameSetFavorite(platform: Platform, gameId: String, value: Boolean) {
        platform.gameList.games.first { it.id == gameId }.favorite = value
        viewModelScope.launch { savePlatform(platform) }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun savePlatform(platform: Platform) = withContext(Dispatchers.IO) {
        val share = share ?: return@withContext
        val holder = GameListHolder(platform.gameList)
        val path = platform.path
        val newJson = gson.toJson(holder)

        val jsonToXml = JsonToXml.Builder(newJson)
            .forceAttribute("/gameList/game/id")
            .forceAttribute("/gameList/game/source")
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

        _stateFlow.value = stateFlow.value.copy(platforms = getPlatforms())
    }

    @Suppress("BlockingMethodInNonBlockingContext")
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
                // ToDo
                session?.close()
                connection?.close()
                null
            }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun getPlatforms(): List<Platform> = withContext(Dispatchers.IO) {
        val platforms = mutableListOf<Platform>()
        val share = share ?: return@withContext emptyList()

        for (file in share.listClean("", "*")) {
            val folderName = file.fileName
            if (share.isFolder("", folderName)) {
                Log.i("TOTO", "Parsing $folderName")
                val filePath = "$folderName\\${GAMELIST_FILE}"
                if (share.fileExists(filePath)) {
                    Log.i("TOTO", "Game list found for $folderName")
                    val readFile = share.openFile(
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
                    Log.i("TOTO", "Game list retrieved with ${holder.gameList.games.size} games")

                    platforms.add(Platform(holder.gameList, filePath))
                }
            }
        }
        platforms
    }

    private fun DiskShare.isFolder(path: String, fileName: String): Boolean =
        folderExists("$path\\$fileName")

    private fun DiskShare.listClean(path: String, pattern: String) =
        list(path, pattern).filter { it.fileName != "." && it.fileName != ".." }

    data class State(
        val sources: List<Source> = emptyList(),
        val platforms: List<Platform> = emptyList(),
    )

    companion object {
        private const val GAMELIST_FILE = "gamelist.xml"
    }
}