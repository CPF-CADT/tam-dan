package com.tamdan.mobile.runtime

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

interface EventStorePersistence {
    fun read(): String?
    fun write(value: String)
    fun clear()
}

interface EventSerialization {
    fun decode(raw: String): List<ActivityEvent>
    fun encode(events: List<ActivityEvent>): String
}

private object JsonEventSerialization : EventSerialization {
    override fun decode(raw: String): List<ActivityEvent> {
        val array = JSONArray(raw)
        return buildList(array.length()) {
            for (index in 0 until array.length()) add(fromJson(array.getJSONObject(index)))
        }
    }

    override fun encode(events: List<ActivityEvent>): String {
        val array = JSONArray()
        events.forEach { event ->
            array.put(JSONObject().apply {
                put("eventId", event.eventId)
                put("eventType", event.eventType)
                put("occurredAt", event.occurredAtEpochMillis)
                put("packageName", event.packageName)
                put("domain", event.domain)
                put("payload", event.payload)
            })
        }
        return array.toString()
    }

    private fun fromJson(value: JSONObject) = ActivityEvent(
        eventId = value.getString("eventId"),
        eventType = value.getString("eventType"),
        occurredAtEpochMillis = value.getLong("occurredAt"),
        packageName = value.optString("packageName").takeIf { it.isNotEmpty() },
        domain = value.optString("domain").takeIf { it.isNotEmpty() },
        payload = value.optString("payload", "{}"),
    )
}

private class SharedPreferencesEventPersistence(context: Context) : EventStorePersistence {
    private val preferences = context.getSharedPreferences("tamdan_activity_events", Context.MODE_PRIVATE)

    override fun read(): String? = preferences.getString(KEY, null)

    override fun write(value: String) {
        preferences.edit().putString(KEY, value).apply()
    }

    override fun clear() {
        preferences.edit().remove(KEY).apply()
    }

    companion object {
        private const val KEY = "pending_events"
    }
}

class ActivityEventStore(
    private val persistence: EventStorePersistence,
    private val maxEvents: Int = DEFAULT_MAX_EVENTS,
    private val nowMillis: () -> Long = { System.currentTimeMillis() },
    private val serialization: EventSerialization = JsonEventSerialization,
) {
    private val lock = ReentrantLock()

    init {
        require(maxEvents in 1..MAX_ALLOWED_EVENTS) { "maxEvents out of bounds" }
    }

    constructor(context: Context) : this(SharedPreferencesEventPersistence(context.applicationContext))

    fun enqueue(event: ActivityEvent): Result<Unit> = lock.withLock {
        event.validate(nowMillis())?.let { return Result.failure(IllegalArgumentException(it)) }
        val events = readEvents()
        if (events.any { it.eventId == event.eventId }) return Result.success(Unit)
        if (events.size >= maxEvents) return Result.failure(IllegalStateException("event queue is full"))
        writeEvents(events + event)
        Result.success(Unit)
    }

    fun pendingBatch(limit: Int): List<ActivityEvent> = lock.withLock {
        require(limit in 1..MAX_BATCH_SIZE) { "limit out of bounds" }
        readEvents().take(limit)
    }

    fun markUploaded(eventIds: Set<String>) = lock.withLock {
        if (eventIds.isEmpty()) return@withLock
        writeEvents(readEvents().filterNot { it.eventId in eventIds })
    }

    fun discardExpired(cutoffEpochMillis: Long) = lock.withLock {
        writeEvents(readEvents().filter { it.occurredAtEpochMillis >= cutoffEpochMillis })
    }

    fun size(): Int = lock.withLock { readEvents().size }

    private fun readEvents(): List<ActivityEvent> {
        val raw = persistence.read() ?: return emptyList()
        return try {
            serialization.decode(raw)
        } catch (_: Exception) {
            persistence.clear()
            emptyList()
        }
    }

    private fun writeEvents(events: List<ActivityEvent>) {
        persistence.write(serialization.encode(events.take(maxEvents)))
    }

    companion object {
        const val DEFAULT_MAX_EVENTS = 500
        const val MAX_ALLOWED_EVENTS = 5_000
        const val MAX_BATCH_SIZE = 100
    }
}
