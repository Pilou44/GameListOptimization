package com.wechantloup.gamelistoptimization.scraper.model

data class ScraperSystem(
    val id: Int,
    val euName: String,
    val usName: String,
    val systemNames: List<String>,
    val extensions: List<String>,
)
