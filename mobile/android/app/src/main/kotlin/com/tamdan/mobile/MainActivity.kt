package com.tamdan.mobile

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import com.tamdan.mobile.runtime.CapabilityMatrix
import com.tamdan.mobile.runtime.DeviceCredentialStore
import com.tamdan.mobile.runtime.DeviceIdentityStore
import com.tamdan.mobile.runtime.MonitoringEngine
import com.tamdan.mobile.runtime.PermissionGateway

class MainActivity : FlutterActivity() {
    private lateinit var monitoringEngine: MonitoringEngine
    private lateinit var credentialStore: DeviceCredentialStore
    private lateinit var identityStore: DeviceIdentityStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        credentialStore = DeviceCredentialStore(applicationContext)
        identityStore = DeviceIdentityStore(applicationContext)
        monitoringEngine = MonitoringEngine(AndroidPermissionGateway(this))
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "getDeviceIdentity" -> {
                        val identity = identityStore.readOrCreate()
                        result.success(
                            mapOf(
                                "identifier" to identity.identifier,
                                "platform" to identity.platform,
                            ),
                        )
                    }
                    "getCapabilities" -> result.success(capabilitiesMap(monitoringEngine.getCapabilities()))
                    "getStatus" -> result.success(statusMap(monitoringEngine.getStatus()))
                    "startMonitoring" -> result.success(statusMap(monitoringEngine.startMonitoring().getOrThrow()))
                    "stopMonitoring" -> result.success(statusMap(monitoringEngine.stopMonitoring().getOrThrow()))
                    "clearDeviceContext" -> {
                        credentialStore.clear()
                        result.success(null)
                    }
                    "openUsageAccessSettings" -> {
                        startActivity(Settings.ACTION_USAGE_ACCESS_SETTINGS.let { android.content.Intent(it) })
                        result.success(null)
                    }
                    else -> result.notImplemented()
                }
            }
    }

    override fun onResume() {
        super.onResume()
        if (::monitoringEngine.isInitialized) monitoringEngine.onPermissionChanged()
    }

    private fun capabilitiesMap(value: CapabilityMatrix) = mapOf(
        "activityObservation" to value.activityObservation.name,
        "applicationPolicy" to value.applicationPolicy.name,
        "gamePolicy" to value.gamePolicy.name,
        "domainObservation" to value.domainObservation.name,
        "domainPolicy" to value.domainPolicy.name,
        "vpnEnforcement" to value.vpnEnforcement.name,
    )

    private fun statusMap(value: com.tamdan.mobile.runtime.MonitoringStatus) = mapOf(
        "state" to value.state.name,
        "permission" to value.permission.name,
        "sync" to value.sync.name,
        "reason" to value.reason,
        "capabilities" to capabilitiesMap(value.capabilities),
    )

    private class AndroidPermissionGateway(private val context: Context) : PermissionGateway {
        override fun hasRequiredPermissions(): Boolean {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val usageAllowed = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName,
            ) == AppOpsManager.MODE_ALLOWED
            val notificationsAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
            return usageAllowed && notificationsAllowed
        }
    }

    companion object {
        private const val CHANNEL = "com.tamdan.mobile/android_runtime"
    }
}
