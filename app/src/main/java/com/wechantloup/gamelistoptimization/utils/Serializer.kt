package com.wechantloup.gamelistoptimization.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder

private val defaultSerializer: Gson = GsonBuilder().create()

/** Serializes the receiver object with the default [Gson] implementation.  */
fun <T> T.serialize(): String = defaultSerializer.toJson(this)
fun <T> String.deserialize(clazz: Class<T>): T = defaultSerializer.fromJson(this, clazz)
inline fun <reified T> String.deserialize(): T = deserialize(T::class.java)
