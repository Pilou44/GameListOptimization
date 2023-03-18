package com.wechantloup.gamelistoptimization.utils

fun String.isEuRegion(): Boolean {
    val euCountries = listOf(
        "BE", "EL", "LT", "PT", "BG", "ES", "LU", "RO", "CZ", "FR", "HU", "SI", "DK", "HR",
        "MT", "SK", "DE", "IT", "NL", "FI", "EE", "CY", "AT", "SE", "IE", "LV", "PL", "UK",
        "CH", "NO", "IS", "LI"
    )
    return euCountries.contains(this.uppercase())
}

fun String.isUsRegion(): Boolean {
    val usCountries = listOf(
        "US", "CA",
    )
    return usCountries.contains(this.uppercase())
}

fun String.isJpRegion(): Boolean {
    val jpCountries = listOf(
        "JP",
    )
    return jpCountries.contains(this.uppercase())
}

fun String.getRegion(): String? {
    return when {
        isEuRegion() -> "eu"
        isUsRegion() -> "us"
        isJpRegion() -> "jp"
        else -> null
    }
}
