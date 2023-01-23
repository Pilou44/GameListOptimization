package com.wechantloup.gamelistoptimization.model

import com.wechantloup.gamelistoptimization.compose.DropdownComparable

data class Source(
    val name: String,
    val ip: String,
    val path: String,
    val login: String,
    val password: String
): DropdownComparable {

    override fun toString(): String = name

    companion object {
        val NAS = Source(
            name = "NAS",
            ip = "192.168.44.104",
            path = "Emulation",
            login = "emulation",
            password = "fPwJ\$\""
        )
        val ROBIN = Source(
            name = "Robin",
            ip = "192.168.44.122",
            path = "roms",
            login = "",
            password = ""
        )
        val MANON = Source(
            name = "Manon",
            ip = "192.168.44.138",
            path = "roms",
            login = "",
            password = ""
        )
        val GUILLAUME = Source(
            name = "Guillaume",
            ip = "192.168.44.120",
            path = "roms",
            login = "",
            password = ""
        )
    }
}

enum class Sources(val source: Source) {
    NAS(Source.NAS),
    ROBIN(Source.ROBIN),
    MANON(Source.MANON),
    GUILLAUME(Source.GUILLAUME),
}
