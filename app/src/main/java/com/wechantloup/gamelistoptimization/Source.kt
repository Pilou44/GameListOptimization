package com.wechantloup.gamelistoptimization

class Source(
    val name: String,
    val ip: String,
    val path: String,
    val login: String,
    val password: String
) {

    override fun toString(): String = name

    companion object {
        val NAS = Source(
            name = "NAS",
            ip = "192.168.44.104",
            path = "Emulation",
            login = "emulation",
            password = "fPwJ\$\""
        )
        val RETROPIE = Source(
            name = "Retropie 4",
            ip = "192.168.44.122",
            path = "roms",
            login = "",
            password = ""
        )
    }
}