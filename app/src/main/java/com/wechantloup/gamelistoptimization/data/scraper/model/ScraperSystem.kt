package com.wechantloup.gamelistoptimization.data.scraper.model

data class ScraperSystem(
    val id: Int,
    val euName: String?,
    val usName: String?,
    val jpName: String?,
    val systemNames: List<String>,
    val extensions: List<String>,
)
