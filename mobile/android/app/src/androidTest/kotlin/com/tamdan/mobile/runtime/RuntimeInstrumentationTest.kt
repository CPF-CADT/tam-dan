package com.tamdan.mobile.runtime

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RuntimeInstrumentationTest {
    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun clearPersistentState() {
        context.getSharedPreferences("tamdan_activity_events", Context.MODE_PRIVATE)
            .edit().clear().commit()
        DeviceCredentialStore(context).clear()
    }

    @After
    fun removePersistentState() {
        context.getSharedPreferences("tamdan_activity_events", Context.MODE_PRIVATE)
            .edit().clear().commit()
        DeviceCredentialStore(context).clear()
    }

    @Test
    fun keystoreCredentialsSurviveStoreRecreationAndClear() {
        val expected = DeviceContext(41, 7, "access-token", "refresh-token")
        DeviceCredentialStore(context).save(expected)

        val recreatedStore = DeviceCredentialStore(context)
        assertEquals(expected, recreatedStore.read())

        recreatedStore.clear()
        assertNull(DeviceCredentialStore(context).read())
    }

    @Test
    fun eventQueueSurvivesStoreRecreationAndAcknowledgesUpload() {
        val event = ActivityEvent(
            eventId = "instrumented_event_1",
            eventType = "app.foreground",
            occurredAtEpochMillis = System.currentTimeMillis(),
            packageName = context.packageName,
        )
        assertTrue(ActivityEventStore(context).enqueue(event).isSuccess)

        val recreatedStore = ActivityEventStore(context)
        assertEquals(listOf(event), recreatedStore.pendingBatch(10))
        recreatedStore.markUploaded(setOf(event.eventId))
        assertEquals(0, ActivityEventStore(context).size())
    }

    @Test
    fun permissionRemovalAndRestorationAreExplicitStates() {
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
    fun synchronizationRecoversFromOfflineToComplete() {
        val coordinator = SynchronizationCoordinator()
        assertEquals(SyncState.PENDING, coordinator.markPending())
        assertEquals(SyncState.SYNCING, coordinator.begin())
        assertEquals(SyncState.OFFLINE, coordinator.onNetworkLost())
        assertEquals(SyncState.PENDING, coordinator.recover())
        assertEquals(SyncState.COMPLETE, coordinator.onUploadSucceeded())
    }

    @Test
    fun policyAcknowledgementRequiresExactVersionAndUnsupportedCapabilitiesStayUnsupported() {
        val engine = PolicyEnforcementEngine(CapabilityMatrix())
        assertEquals(
            EnforcementState.PENDING_DELIVERY,
            engine.apply(Policy(10, 3, PolicyType.APPLICATION)).state,
        )
        assertEquals(
            EnforcementState.FAILED,
            engine.acknowledge(Policy(10, 2, PolicyType.APPLICATION)).state,
        )
        assertEquals(
            EnforcementState.ACTIVE,
            engine.acknowledge(Policy(10, 3, PolicyType.APPLICATION)).state,
        )
        assertEquals(
            EnforcementState.UNSUPPORTED,
            engine.apply(Policy(11, 1, PolicyType.DOMAIN)).state,
        )
        assertEquals(
            EnforcementState.UNSUPPORTED,
            engine.apply(Policy(12, 1, PolicyType.VPN)).state,
        )
    }

    @Test
    fun targetApplicationIsAvailableOnTheEmulator() {
        assertNotNull(context.packageManager.getApplicationInfo(context.packageName, 0))
    }

    @Test
    fun deviceIdentitySurvivesStoreRecreationWithoutUsingPlaceholder() {
        val first = DeviceIdentityStore(context).readOrCreate()
        val recreated = DeviceIdentityStore(context).readOrCreate()

        assertEquals(first, recreated)
        assertEquals(DeviceIdentity.PLATFORM_ANDROID, first.platform)
        assertTrue(DeviceIdentityRules.isValidIdentifier(first.identifier))
        assertFalse(first.identifier.equals("flutter-device", ignoreCase = true))
    }
}
