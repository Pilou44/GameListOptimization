package com.wechantloup.gamelistoptimization.data.scraper

import retrofit2.HttpException

class TooManyRequestsException(msg: String, cause: HttpException): Exception(msg, cause)
class UnknownGameException(msg: String, cause: Exception? = null): Exception(msg, cause)
class BadCrcException(msg: String, cause: HttpException): Exception(msg, cause)
