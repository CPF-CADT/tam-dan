package com.tamdan.mobile.runtime

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeEngineTest {
    @Test
    fun monitoringRequiresPermissionAndRecoversAfterPermissionIsRestored() {
        var granted = false
        val engine = MonitoringEngine(object : PermissionGateway {
            override fun hasRequiredPermissions() = granted
        })

        assertEquals(MonitoringState.PENDING, engine.startMonitoring().getOrThrow().state)
        granted = true
        assertEquals(MonitoringState.ACTIVE, engine.startMonitoring().getOrThrow().state)
        granted = false
        assertEquals(MonitoringState.FAILED, engine.onPermissionChanged().state)
        granted = true
        assertEquals(MonitoringState.ACTIVE, engine.startMonitoring().getOrThrow().state)
    }

    @Test
    fun unsupportedDomainAndVpnNeverBecomeActive() {
        val engine = PolicyEnforcementEngine(CapabilityMatrix())
        assertEquals(EnforcementState.UNSUPPORTED, engine.apply(Policy(1, 1, PolicyType.DOMAIN)).state)
        assertEquals(EnforcementState.UNSUPPORTED, engine.apply(Policy(2, 1, PolicyType.VPN)).state)
    }

    @Test
    fun policyRequiresExactVersionAcknowledgement() {
        val engine = PolicyEnforcementEngine(CapabilityMatrix())
        assertEquals(EnforcementState.PENDING_DELIVERY, engine.apply(Policy(1, 4, PolicyType.APPLICATION)).state)
        assertEquals(EnforcementState.FAILED, engine.acknowledge(Policy(1, 3, PolicyType.APPLICATION)).state)
        assertEquals(EnforcementState.ACTIVE, engine.acknowledge(Policy(1, 4, PolicyType.APPLICATION)).state)
        assertEquals(EnforcementState.REMOVED, engine.remove(1).state)
    }

    @Test
    fun synchronizationHasExplicitOfflineRecoveryStates() {
        val sync = SynchronizationCoordinator()
        assertEquals(SyncState.PENDING, sync.markPending())
        assertEquals(SyncState.SYNCING, sync.begin())
        assertEquals(SyncState.OFFLINE, sync.onNetworkLost())
        assertEquals(SyncState.PENDING, sync.recover())
        assertEquals(SyncState.COMPLETE, sync.onUploadSucceeded())
    }

    @Test
    fun eventStoreIsBoundedIdempotentAndValidatesPayload() {
        val persistence = MemoryPersistence()
        var now = 1_000_000L
        val store = ActivityEventStore(
            persistence,
            maxEvents = 2,
            nowMillis = { now },
            serialization = PlainEventSerialization,
        )
        val first = ActivityEvent("event_1", "app.foreground", now - 1)
        assertTrue(store.enqueue(first).isSuccess)
        assertTrue(store.enqueue(first).isSuccess)
        assertEquals(1, store.size())
        assertTrue(store.enqueue(ActivityEvent("event_2", "app.foreground", now)).isSuccess)
        assertFalse(store.enqueue(ActivityEvent("event_3", "app.foreground", now)).isSuccess)
        assertFalse(store.enqueue(ActivityEvent("bad id", "app.foreground", now)).isSuccess)
        store.markUploaded(setOf("event_1"))
        assertEquals(listOf("event_2"), store.pendingBatch(10).map { it.eventId })
        now += 100
        store.discardExpired(now)
        assertEquals(0, store.size())
    }

    private class MemoryPersistence : EventStorePersistence {
        private var value: String? = null
        override fun read() = value
        override fun write(value: String) { this.value = value }
        override fun clear() { value = null }
    }

    private object PlainEventSerialization : EventSerialization {
        override fun decode(raw: String): List<ActivityEvent> = raw.split(";").filter { it.isNotEmpty() }.map {
            val fields = it.split("|", limit = 6)
            ActivityEvent(fields[0], fields[1], fields[2].toLong(), fields[3].ifEmpty { null },
                fields[4].ifEmpty { null }, fields[5])
        }

        override fun encode(events: List<ActivityEvent>): String = events.joinToString(";") {
            listOf(it.eventId, it.eventType, it.occurredAtEpochMillis, it.packageName ?: "", it.domain ?: "", it.payload)
                .joinToString("|")
        }
    }
}
