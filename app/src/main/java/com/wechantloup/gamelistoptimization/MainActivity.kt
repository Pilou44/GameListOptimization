package com.wechantloup.gamelistoptimization

import android.os.Bundle
import android.util.Log
import android.util.Xml
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import com.hierynomus.smbj.share.File
import com.wechantloup.gamelistoptimization.databinding.ActivityMainBinding
import fr.arnaudguyon.xmltojsonlib.XmlToJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.util.EnumSet
import fr.arnaudguyon.xmltojsonlib.JsonToXml




class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPostResume() {
        super.onPostResume()

        lifecycleScope.launch {
            parseRoot()
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun parseRoot() = withContext(Dispatchers.IO) {
        val client = SMBClient()

        Log.i("TOTO", "Connect to SAMBA server")
        client.connect(NAS_IP).use { connection ->
            val ac = AuthenticationContext(NAS_LOGIN, NAS_PWD.toCharArray(), "DOMAIN")
            Log.i("TOTO", "Try to authenticate")
            val session: Session = connection.authenticate(ac)
            Log.i("TOTO", "Session created")
            (session.connectShare("Emulation") as DiskShare).use { share ->
//                for (f in share.listClean("", "*")) {
//                    println("File : ${f.fileName} attributes : ${f.fileAttributes} isFolder : ${share.isFolder("", f.fileName)}")
//                }
//                for (f in share.listClean("snes", "*")) {
//                    println("File : ${f.fileName} attributes : ${f.fileAttributes} isFolder : ${share.isFolder("snes", f.fileName)}")
//                }

                for (f in share.listClean("", "*")) {
                    val folderName = f.fileName
                    if (share.isFolder("", folderName)) {
                        Log.i("TOTO", "Parsing $folderName")
                        val filePath = "$folderName\\$GAMELIST_FILE"
                        if (share.fileExists(filePath)) {
                            Log.i("TOTO", "Game list found for $folderName")
                            val file = share.openFile(
                                filePath,
                                EnumSet.of(AccessMask.GENERIC_READ),
                                null,
                                SMB2ShareAccess.ALL,
                                SMB2CreateDisposition.FILE_OPEN,
                                null)

//                            val inputreader = InputStreamReader(file.inputStream)
//                            val buffreader = BufferedReader(inputreader)
//                            var content = ""
//                            try {
//                                var line = buffreader.readLine()
//                                while (line != null) {
//                                    content += "$line\n"
//                                    line = buffreader.readLine()
//                                }
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                            }

//                            parse(file.inputStream)
                            val inputStream = file.inputStream
                            val xmlToJson: XmlToJson = XmlToJson.Builder(inputStream, null).build()
                            inputStream.close()

                            val jsonString = xmlToJson.toString()

                            val gson = Gson()
                            val holder = gson.fromJson(jsonString, GameListHolder::class.java)
                            Log.i("TOTO", "Game list retrieved with ${holder.gameList.game.size} games")

                            val newJson = gson.toJson(holder)

                            val jsonToXml = JsonToXml.Builder(newJson)
                                .forceAttribute("/gameList/game/id")
                                .forceAttribute("/gameList/game/source")
                                .build()
                            val newXml = jsonToXml.toFormattedString(2)

                            Log.i("TOTO", "New xml = $newXml")
                        }
                    }
                }
            }
        }
    }

//    @Throws(XmlPullParserException::class, IOException::class)
//    private fun parse(inputStream: InputStream): List<*> {
//        inputStream.use { inputStream ->
//            val parser: XmlPullParser = Xml.newPullParser()
//            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
//            parser.setInput(inputStream, null)
//            parser.nextTag()
//            return readFeed(parser)
//        }
//    }
//
//    @Throws(XmlPullParserException::class, IOException::class)
//    private fun readFeed(parser: XmlPullParser): List<Entry> {
//        val entries = mutableListOf<Entry>()
//
//        parser.require(XmlPullParser.START_TAG, ns, "feed")
//        while (parser.next() != XmlPullParser.END_TAG) {
//            if (parser.eventType != XmlPullParser.START_TAG) {
//                continue
//            }
//            // Starts by looking for the entry tag
//            if (parser.name == "entry") {
//                entries.add(readEntry(parser))
//            } else {
//                skip(parser)
//            }
//        }
//        return entries
//    }

    private fun DiskShare.isFolder(path: String, fileName: String): Boolean =
        folderExists("$path\\$fileName")

    private fun DiskShare.listClean(path: String, pattern: String) =
        list(path, pattern).filter { it.fileName != "." && it.fileName != ".." }

    companion object {
        private const val NAS_IP = "192.168.44.104"
        private const val NAS_LOGIN = "emulation"
        private const val NAS_PWD = "fPwJ\$\""
        private const val GAMELIST_FILE = "gamelist.xml"
    }
}