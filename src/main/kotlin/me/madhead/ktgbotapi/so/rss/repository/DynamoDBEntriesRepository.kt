package me.madhead.ktgbotapi.so.rss.repository

import me.madhead.ktgbotapi.so.rss.model.Entry
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class DynamoDBEntriesRepository(
    private val dynamoDb: DynamoDbClient,
    private val table: String
) : EntriesRepository {
    override fun get(id: String): Entry? =
        dynamoDb.getItem {
            it.tableName(table)
            it.key(mapOf("id" to AttributeValue.fromS(id)))
        }.item()?.takeUnless { it.isEmpty() }?.toEntry()

    override fun save(value: Entry) {
        dynamoDb.putItem {
            it.tableName(table)
            it.item(value.toAttributes())
        }
    }
}

fun Entry.toAttributes(): Map<String, AttributeValue> = mapOf(
    "id" to AttributeValue.fromS(this.id),
)

fun Map<String, AttributeValue>.toEntry(): Entry = Entry(
    id = this["id"]?.s() ?: throw IllegalStateException("Missing id property")
)
