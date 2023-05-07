package me.madhead.ktgbotapi.so.rss

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.api.telegramBot
import dev.inmo.tgbotapi.types.ChatIdWithThreadId
import dev.inmo.tgbotapi.types.message.textsources.TextLinkTextSource
import kotlinx.coroutines.runBlocking
import me.madhead.ktgbotapi.so.rss.model.Entry
import me.madhead.ktgbotapi.so.rss.repository.DynamoDBEntriesRepository
import me.madhead.ktgbotapi.so.rss.repository.EntriesRepository
import org.apache.logging.log4j.LogManager
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.lang.System.getenv
import java.net.URL

class Handler : RequestHandler<ScheduledEvent, Unit> {
    companion object {
        private val logger = LogManager.getLogger(Handler::class.java)!!

        private const val RSS_URL = "https://stackoverflow.com/feeds/tag/ktgbotapi"

        private val dynamo = DynamoDbClient.create()
        private val entriesRepository: EntriesRepository = DynamoDBEntriesRepository(dynamo, getenv("TABLE_ENTRIES"))

        private val bot = telegramBot(getenv("TOKEN_TELEGRAM"))
    }


    override fun handleRequest(input: ScheduledEvent, context: Context) = runBlocking {
        logger.info("Checking for the updates…")

        val feed = SyndFeedInput().build(XmlReader(URL(RSS_URL)))

        feed.entries.forEach { atomEntry ->
            if (entriesRepository.get(atomEntry.uri) == null) {
                logger.info("Found a new entry: ${atomEntry.uri}")

                entriesRepository.save(Entry(atomEntry.uri))

                bot.sendMessage(
                    chatId = ChatIdWithThreadId(
                        chatId = getenv("CHAT_ID").toLong(),
                        threadId = getenv("THREAD_ID").toLong(),
                    ),
                    entities = listOf(
                        TextLinkTextSource(atomEntry.title, atomEntry.uri)
                    ),
                )
            }
        }
    }
}
