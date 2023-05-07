package me.madhead.ktgbotapi.so.rss.repository

import me.madhead.ktgbotapi.so.rss.model.Entry

interface EntriesRepository {
    fun get(id: String): Entry?

    fun save(value: Entry)
}
