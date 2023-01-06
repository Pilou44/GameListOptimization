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
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
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

    private val gameSources = Sources.values().map { it.source } .toList()

    private var share: DiskShare? = null

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
            if (source != getCurrentSource()) {
                if (share?.isConnected == true) {
                    share?.close()
                }
                share = source.connectTo()
            }
            _stateFlow.value = stateFlow.value.copy(
                currentSource = source,
                platforms = getPlatforms(),
            )
        }
    }

    fun setPlatform(selectedPlatform: Platform) {
        _stateFlow.value = stateFlow.value.copy(
            currentPlatform = selectedPlatform,
            games = selectedPlatform.gameList.getGamesCopy(),
            hasBackup = selectedPlatform.gameListBackup != null,
        )
    }

    fun onGameSetForKids(gamePath: String, value: Boolean) {
        val platform = getCurrentPlatform() ?: return
        platform.gameList.games.first { it.path == gamePath }.kidgame = value
        viewModelScope.launch { savePlatform(platform) }
    }

    fun onGameSetFavorite(gamePath: String, value: Boolean) {
        val platform = getCurrentPlatform() ?: return
        platform.gameList.games.first { it.path == gamePath }.favorite = value
        viewModelScope.launch { savePlatform(platform) }
    }

    fun copyBackupValues() {
        val platform = getCurrentPlatform() ?: return
        val gameListBackup = platform.gameListBackup ?: return
        platform.gameList.games.forEach { game ->
            val backup = gameListBackup.games.firstOrNull { it.id == game.id }
            backup?.let {
                game.kidgame = backup.kidgame
                game.favorite = backup.favorite
            }
        }
        viewModelScope.launch { savePlatform(platform) }
    }

    fun setAllFavorite() {
        val platform = getCurrentPlatform() ?: return
        val allFavorite = platform.gameList.games.all { it.favorite == true }
        platform.gameList.games.forEach {
            it.favorite = !allFavorite
        }
        viewModelScope.launch { savePlatform(platform) }
    }

    fun setAllForKids() {
        val platform = getCurrentPlatform() ?: return
        val allForKids = platform.gameList.games.all { it.kidgame == true }
        platform.gameList.games.forEach {
            it.kidgame = !allForKids
        }
        viewModelScope.launch { savePlatform(platform) }
    }

    fun savePlatformName(name: String) {
        val platform = getCurrentPlatform() ?: return
        if (name.isEmpty() || name == platform.gameList.platform) return

        val newGameList = platform.gameList.copy(platform = name)
        val newPlatform = platform.copy(gameList = newGameList)
        viewModelScope.launch { savePlatform(newPlatform) }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun savePlatform(platform: Platform) = withContext(Dispatchers.IO) {
        val share = share ?: return@withContext
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

        val platforms = getPlatforms()
        val newPlatform = platforms.first { it.path == platform.path }

        _stateFlow.value = stateFlow.value.copy(
            platforms = platforms,
            currentPlatform = newPlatform,
            games = newPlatform.gameList.getGamesCopy(),
            hasBackup = newPlatform.gameListBackup != null
        )
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
                e.printStackTrace()
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
                val filePath = "$folderName\\${GAMELIST_FILE}"
                share.extractGameList(folderName, GAMELIST_FILE)?.let { it ->
                    val gameListBackup = share.extractGameList(folderName, GAMELIST_BACKUP_FILE)
                    platforms.add(Platform(it, gameListBackup, filePath))
                }
            }
        }
        platforms.sortedBy { it.toString() }
    }

    private fun getCurrentPlatform(): Platform? = stateFlow.value.currentPlatform
    private fun getCurrentSource(): Source? = stateFlow.value.currentSource

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

    private fun DiskShare.isFolder(path: String, fileName: String): Boolean =
        folderExists("$path\\$fileName")

    private fun DiskShare.listClean(path: String, pattern: String) =
        list(path, pattern).filter { it.fileName != "." && it.fileName != ".." }

    data class State(
        val sources: List<Source> = emptyList(),
        val platforms: List<Platform> = emptyList(),
        val games: List<Game> = emptyList(),
        val hasBackup: Boolean = false,
        val currentSource: Source? = null,
        val currentPlatform: Platform? = null,
    )

    companion object {
        private const val TAG = "MainViewModel"
        private const val GAMELIST_FILE = "gamelist.xml"
        private const val GAMELIST_BACKUP_FILE = "gamelist.backup.xml"
    }
}
