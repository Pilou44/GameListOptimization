package com.wechantloup.gamelistoptimization.model

data class ScrapResult(
    val game: Game,
    val status: Status,
) {
    enum class Status {
        SUCCESS,
        TOO_MANY_REQUESTS,
        UNKNOWN_GAME,
        BAD_CRC,
    }
}
