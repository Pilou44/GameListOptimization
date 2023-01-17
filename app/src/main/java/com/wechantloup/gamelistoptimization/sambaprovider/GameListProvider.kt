package com.wechantloup.gamelistoptimization.sambaprovider

import android.util.Log
import com.google.gson.Gson
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.mssmb2.SMBApiException
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.Directory
import com.hierynomus.smbj.share.DiskShare
import com.wechantloup.gamelistoptimization.model.Game
import com.wechantloup.gamelistoptimization.model.Platform
import com.wechantloup.gamelistoptimization.model.Source
import fr.arnaudguyon.xmltojsonlib.JsonToXml
import fr.arnaudguyon.xmltojsonlib.XmlToJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.EnumSet
import java.util.zip.CRC32
import java.util.zip.CheckedInputStream

class GameListProvider {

    private val gson = Gson()
    private var share: DiskShare? = null
    private var currentSource: Source? = null

    private val String.extension: String?
        get() {
            val dotIndex = lastIndexOf(".")
            if (dotIndex == -1) return null
            return substring(dotIndex + 1)
        }

    suspend fun open(source: Source): Boolean = withContext(Dispatchers.IO) {
        if (source != currentSource) {
            if (share?.isConnected == true) {
                share?.close()
            }
            currentSource = source
            Log.i(TAG, "Connect to ${source.name}")
            share = source.connectTo()
        }
        return@withContext share != null
    }

    suspend fun close() = withContext(Dispatchers.IO) {
        Log.i(TAG, "Disconnect from ${currentSource?.name}")
        currentSource = null
        share?.close()
        share = null
    }

    suspend fun getPlatforms(): List<Platform> = withContext(Dispatchers.IO) {
        val platforms = mutableListOf<Platform>()
        val share = getShare()

        for (file in share.listClean("", "*")) {
            val folderName = file.fileName
            if (share.isFolder("", folderName)) {
                val filePath = "$folderName\\$GAMELIST_FILE"
                share.extractGameList(folderName, GAMELIST_FILE)?.let { it ->
                    val gameListBackup = share.extractGameList(folderName, GAMELIST_BACKUP_FILE)
                    platforms.add(
                        Platform(
                            name = it.provider?.system ?: folderName,
                            games = it.games.map { it.toGame() },
                            gamesBackup = gameListBackup?.games?.map { it.toGame() },
                            path = filePath,
                            system = folderName,
                            extensions = emptyList() // ToDo
                        )
                    )
                }
            }
        }
        platforms.sortedBy { it.toString() }
    }

    suspend fun savePlatform(platform: Platform) = withContext(Dispatchers.IO) {
        val holder = platform.toGameListHolder()
        val path = platform.path
        val newJson = gson.toJson(holder)

        val jsonToXml = JsonToXml.Builder(newJson)
            .forceAttribute("/gameList/game/id")
            .forceAttribute("/gameList/game/source")
            .forceAttribute("/gameList/platform")
            .build()
        val newXml = jsonToXml.toFormattedString(2)

        val outFile = getShare().openFile(
            path,
            EnumSet.of(AccessMask.GENERIC_WRITE),
            null,
            SMB2ShareAccess.ALL,
            SMB2CreateDisposition.FILE_OVERWRITE,
            null,
        )
        val outputStream = outFile.outputStream
        outputStream.use {
            it.write(newXml.toByteArray(Charsets.UTF_8))
        }
        outFile.close()

        Log.i(TAG, "Platform $platform saved")
    }

    suspend fun getGameSize(game: Game, platform: Platform): Long = withContext(Dispatchers.IO) {
        val gamePath = game.getPath(platform)
        val info = getShare().getFileInformation(gamePath)
        return@withContext info.standardInformation.endOfFile
    }

    suspend fun getGameCrc(game: Game, platform: Platform): Long = withContext(Dispatchers.IO) {
        val gamePath = game.getPath(platform)
        val file = getShare().openFile(
            gamePath,
            EnumSet.of(AccessMask.GENERIC_READ),
            null,
            SMB2ShareAccess.ALL,
            SMB2CreateDisposition.FILE_OPEN,
            null,
        )
        val crc = CRC32()
        CheckedInputStream(file.inputStream, crc).use { cis ->
            var read = cis.read()
            while (read != -1) {
                read = cis.read()
            }
        }
        return@withContext crc.value
    }

    suspend fun downloadGameImage(game: Game, platform: Platform, destFile: File): Boolean =
        withContext(Dispatchers.IO) {
            if (game.image == null) return@withContext false

            val imagePath = game.getImagePath(platform)
            Log.d(TAG, "Image path = $imagePath")

            try {
                download(imagePath, destFile)
                return@withContext true
            } catch (e: Exception) {
                Log.w(TAG, "Can't open $imagePath")
                return@withContext false
            }
        }

    suspend fun downloadGame(game: Game, platform: Platform, destFile: File): Boolean =
        withContext(Dispatchers.IO) {
            val gamePath = game.getPath(platform)
            Log.d(TAG, "Game path = $gamePath")

            download(gamePath, destFile)

            return@withContext true
        }

    suspend fun uploadGame(
        game: Game,
        gameFile: File,
        imageFile: File?,
        srcPlatform: Platform,
        destination: Source,
    ): Boolean = withContext(Dispatchers.IO) {
        val savedSource = currentSource

        open(destination)

        val destPlatform =
            getPlatforms().firstOrNull { it.isSameAs(srcPlatform) } ?: return@withContext false // ToDo Create platform

        val gamePath = game.getPath(destPlatform)
        Log.d(TAG, "Game path = $gamePath")
        upload(gameFile, gamePath)
        Log.i(TAG, "Game uploaded")

        if (game.image != null && imageFile != null) {
            val imagePath = game.getImagePath(destPlatform)
            Log.d(TAG, "Image path = $imagePath")
            upload(imageFile, imagePath)
            Log.i(TAG, "Image uploaded")
        }

        val destGames = destPlatform.games.toMutableList()
            .apply { add(game) }
        val newPlatform = destPlatform.copy(games = destGames)
        savePlatform(newPlatform)

        if (savedSource != null) {
            open(savedSource)
        } else {
            close()
        }

        return@withContext true
    }

    suspend fun cleanGameList(platform: Platform): Platform = withContext(Dispatchers.IO) {
        val directory: Directory = getShare().openDirectory(
            platform.system,
            EnumSet.of(AccessMask.FILE_LIST_DIRECTORY),
            EnumSet.of(FileAttributes.FILE_ATTRIBUTE_DIRECTORY),
            SMB2ShareAccess.ALL,
            SMB2CreateDisposition.FILE_OPEN,
            null,
        )
        val files = directory.list()
        Log.d(TAG, "${files.size} files found")

        val games = directory.list()
            .filter { platform.extensions.contains(it.fileName.extension) }
            .map { it.fileName }
        Log.d(TAG, "${games.size} games found")

        val platformGames = platform.games.toMutableList()
        Log.d(TAG, "Platform contains ${platformGames.size} games")
        platform.games.forEach {
            val fileName = it.path.substring(it.path.lastIndexOf("/") + 1)
            if (!games.contains(fileName)) {
                platformGames.remove(it)
            }
        }
        Log.d(TAG, "Platform contains ${platformGames.size} games after removing missing files")

        val platformGameNames = platformGames.map { it.path.substring(it.path.lastIndexOf("/") + 1) }
        games.forEach {
            if (!platformGameNames.contains(it)) {
                platformGames.add(Game(
                    id = null,
                    source = null,
                    path = "./$it",
                    name = null,
                    desc = null,
                    rating = null,
                    releasedate = null,
                    developer = null,
                    publisher = null,
                    genre = null,
                    players = null,
                    image = null,
                    marquee = null,
                    video = null,
                    genreid = null,
                    favorite = null,
                    kidgame = null,
                    hidden = null,
                ))
            }
        }
        Log.d(TAG, "Platform contains ${platformGames.size} games after adding remaining files")
        return@withContext platform.copy(games = platformGames)
    }

    private suspend fun getShare(): DiskShare {
        val diskShare = share ?: throw IllegalStateException("Not connected")
        val source = currentSource ?: throw IllegalStateException("No source defined")

        if (diskShare.isConnected) return diskShare

        share = source.connectTo()

        return requireNotNull(share) { "Unable to reconnect" }
    }

    private fun Game.getImagePath(platform: Platform): String {
        return "${platform.system}\\$image".cleanPath()
    }

    private fun Game.getPath(platform: Platform): String {
        return "${platform.system}\\$path".cleanPath()
    }

    private fun String.cleanPath(): String = replace("/", "\\").replace("\\.\\", "\\")

    private suspend fun upload(file: File, path: String) = withContext(Dispatchers.IO) {
        val share = getShare()

        val parentPath = path.substring(0, path.lastIndexOf("\\"))
        Log.d(TAG, "Parent path = $parentPath")
        share.mkdirs(parentPath)

        val dest = share.openFile(
            path,
            EnumSet.of(AccessMask.GENERIC_WRITE),
            null,
            SMB2ShareAccess.ALL,
            SMB2CreateDisposition.FILE_OVERWRITE_IF,
            null,
        )
        val outputStream = dest.outputStream
        val inputStream = file.inputStream()

        outputStream.use { output ->
            inputStream.use { input ->
                copy(input, output)
            }
        }

        dest.close()
    }

    private suspend fun download(path: String, file: File) = withContext(Dispatchers.IO) {
        val src = getShare().openFile(
            path,
            EnumSet.of(AccessMask.GENERIC_READ),
            null,
            SMB2ShareAccess.ALL,
            SMB2CreateDisposition.FILE_OPEN,
            null
        )
        val inputStream = src.inputStream

        if (!file.exists()) {
            file.createNewFile()
        }
        val outputStream = file.outputStream()

        outputStream.use { output ->
            inputStream.use { input ->
                copy(input, output)
            }
        }

        src.close()
    }

    private fun DiskShare.mkdirs(path: String) {
        var index = path.indexOf("\\")
        var rest = path
        var first = ""
        while (index > 0) {
            first += rest.substring(0, index)
            rest = rest.substring(index + 1)
            Log.d(TAG, "mkdir($first)")
            createDir(first)
            first += "\\"
            index = rest.indexOf("\\")
        }
        Log.d(TAG, "mkdir($path)")
        createDir(path)
    }

    private fun DiskShare.createDir(path: String) = try {
        mkdir(path)
    } catch (e: SMBApiException) {
        if (e.statusCode == 0xc0000035) {
            Log.d(TAG, "$path already exists")
        } else {
            throw e
        }
    }

    private fun copy(source: InputStream, target: OutputStream) {
        val buf = ByteArray(8192)
        var length: Int
        while (source.read(buf).also { length = it } != -1) {
            target.write(buf, 0, length)
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

    private fun GameListGame.toGame(): Game {
        return Game(
            id = id,
            source = source,
            path = path,
            name = name,
            desc = desc,
            rating = rating,
            releasedate = releasedate,
            developer = developer,
            publisher = publisher,
            genre = genre,
            players = players,
            image = image,
            marquee = marquee,
            video = video,
            genreid = genreid,
            favorite = favorite,
            kidgame = kidgame,
            hidden = hidden,
        )
    }

    private fun Game.toGameListGame(): GameListGame {
        return GameListGame(
            id = id,
            source = source,
            path = path,
            name = name,
            desc = desc,
            rating = rating,
            releasedate = releasedate,
            developer = developer,
            publisher = publisher,
            genre = genre,
            players = players,
            image = image,
            marquee = marquee,
            video = video,
            genreid = genreid,
            favorite = favorite,
            kidgame = kidgame,
            hidden = hidden,
        )
    }

    private fun Platform.toGameListHolder(): GameListHolder {
        val games = games.map { it.toGameListGame() }
        val provider = Provider(
            system = name,
            software = null,
            database = null,
            web = null,
        )
        val gameList = GameList(
            games = games,
            provider = provider,
        )
        return GameListHolder(gameList)
    }

    companion object {

        private const val TAG = "GameListProvider"

        const val GAMELIST_FILE = "gamelist.xml"
        private const val GAMELIST_BACKUP_FILE = "gamelist.backup.xml"
    }
}
