# PRD — Child Mobile Application

## 1. Purpose

Build the Flutter application installed on the child device. It provides pairing, consent, permission guidance, monitoring visibility, synchronization status, and policy status.

Low-level monitoring and enforcement are implemented by the separate Kotlin Android engine.

## 2. Primary User

The child-device user and the parent or guardian assisting with initial setup.

## 3. Goals

- Make monitoring transparent and understandable.
- Guide the user through secure device pairing.
- Guide the user through required Android permissions.
- Show whether monitoring, synchronization, and restrictions are working.
- Connect the Flutter interface to the Kotlin engine through a stable contract.

## 4. Core Features

### Initial Setup

- Display a clear product and privacy introduction.
- Explain which data is collected and which data is not collected.
- Require acknowledgement before pairing begins.
- Accept a parent-generated pairing code.
- Display pairing success, expiry, invalid-code, and network-error states.
- Store the issued device credential securely.

### Permission Guidance

- Explain why usage access is needed.
- Open the appropriate Android usage-access settings screen.
- Explain why VPN approval is needed.
- Start the Android VPN-consent flow through the native engine.
- Display granted, denied, unavailable, and removed states.
- Allow the user to retry a missing permission.
- Avoid misleading the user when a capability remains inactive.

### Monitoring Status

- Show whether activity monitoring is running.
- Show whether domain observation is available.
- Show whether the VPN service is running.
- Show the latest successful collection time.
- Show monitoring gaps or stopped services.
- Provide recovery instructions when appropriate.

### Synchronization Status

- Show whether the device is online or offline.
- Show the latest successful synchronization time.
- Show whether events are waiting to upload.
- Allow an appropriate manual retry.
- Show authentication, network, and backend failures separately.

### Policy Status

- Display the latest received policy version.
- List active application, game, and domain restrictions.
- Display schedule information in the device's local time zone.
- Show `PENDING`, `ACTIVE`, `FAILED`, or `UNSUPPORTED` status.
- Explain that enforcement results are reported to the parent.

### Device Identity

- Display the child profile and device name associated with the pairing.
- Support an authorized device rename when allowed by the backend.
- Detect revoked or expired device credentials.
- Clear protected local state after confirmed revocation.
- Return to setup when the device is no longer paired.

### Native Engine Communication

- Read permission and monitoring status.
- Request monitoring start or recovery.
- Read synchronization and queue status.
- Pass authenticated synchronization configuration securely.
- Receive policy state and enforcement results.
- Handle unavailable, timeout, and native-error responses.

## 5. Main Screens

- Product introduction and privacy summary.
- Pairing-code entry.
- Usage-access permission guidance.
- VPN permission guidance.
- Setup-completion summary.
- Device status dashboard.
- Active-policy list.
- Monitoring and synchronization troubleshooting.
- Device information and unpair state.

## 6. Flutter-to-Kotlin Contract

The application requires a versioned platform-channel contract for:

- `getPermissionStatus`
- `requestVpnPermission`
- `startMonitoring`
- `stopMonitoring`
- `getMonitoringStatus`
- `getSyncStatus`
- `getPolicyStatus`
- `retrySynchronization`
- Native status and error events

Exact request, response, error, and version formats must be agreed with Task 3 before implementation.

## 7. Required Backend Contracts

- Pairing-code submission.
- Device credential refresh or rotation where supported.
- Device status and profile information.
- Policy download.
- Device health reporting.
- Device revocation handling.

Backend and native dependencies must be represented by mocks so UI work can continue independently.

## 8. Business Rules

- Monitoring must not begin before required setup and permission steps.
- The user must be able to see when monitoring is inactive.
- The application must not claim that a failed restriction is active.
- The child interface must not expose sensitive parent-account information.
- The application must continue to show the most recent known policy while offline.
- Revoked credentials must no longer upload data or fetch policies.

## 9. Security and Privacy

- Store device credentials using Android secure storage.
- Do not store the parent's password or access token.
- Do not display private activity content on the child status screen.
- Do not collect messages, keystrokes, passwords, or decrypted HTTPS content.
- Do not hide monitoring from the device user.
- Remove protected local data after verified device revocation according to retention rules.

## 10. Acceptance Criteria

The task is complete when:

- A physical Android device can complete the pairing flow.
- Permission guidance reaches the correct Android settings or consent screen.
- The application accurately displays native monitoring and VPN states.
- Active policies and enforcement outcomes are understandable.
- Offline, revoked, expired, denied-permission, and server-error states are handled.
- Flutter-to-Kotlin contract tests pass against the Task 3 implementation.
- Core setup and status flows pass widget, integration, accessibility, and physical-device tests.

## 11. Out of Scope

- Direct activity collection in Dart.
- VPN packet processing in Flutter.
- Parent reporting and policy-editing interfaces.
- ML training or classification.
- Reading private application content.
