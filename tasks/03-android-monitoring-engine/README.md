# PRD — Android Monitoring and Enforcement Engine

## 1. Purpose

Build the native Kotlin engine responsible for permitted activity collection, supported domain observation, offline event storage, synchronization support, and local policy enforcement on the child Android device.

The engine exposes a versioned interface to the child Flutter application and does not contain parent-facing UI or ML training.

## 2. Goals

- Collect reliable activity data with explicit Android permission.
- Observe only technically supported domain signals.
- Continue safe operation during temporary network loss.
- Apply supported policies locally and report the exact result.
- Minimize battery, memory, network, and privacy impact.
- Make every failure visible to the Flutter application and backend.

## 3. Core Features

### Usage-Access Management

- Detect whether Android usage access is granted.
- Expose permission state to Flutter.
- Provide the correct intent for the Android settings flow.
- Detect when permission is removed.
- Stop affected collection cleanly when permission is unavailable.

### Application-Usage Collection

- Query permitted Android application-usage events.
- Calculate or provide data required for application usage duration.
- Record package name, event type, event time, and required metadata.
- Avoid collecting application content.
- Handle device restart, locked-device periods, clock changes, and missed windows.
- Prevent repeated collection from creating duplicate events.

### Foreground-Application Transition Signals

- Record supported foreground-application transitions.
- Produce evidence for possible Facebook, browser, and Telegram invitation activity.
- Record timestamps and application package names.
- Add supported `t.me` domain evidence when available.
- Label the output as a possible transition, not a confirmed group join.
- Avoid collecting screen content, group names, or messages.

### Domain Observation

- Manage the approved local VPN tunnel.
- Observe supported DNS or domain signals without decrypting HTTPS.
- Normalize domain names before storage.
- Record observation time and required device context.
- Avoid claiming visibility when encrypted DNS or protocol behavior prevents observation.
- Handle VPN startup, shutdown, restart, and failure.

### Offline Event Queue

- Assign a stable unique identifier to every event.
- Store events durably before upload.
- Batch events according to size and time limits.
- Retry failed uploads with controlled backoff.
- Remove events only after backend acknowledgement.
- Preserve queued events across application and device restarts.
- Enforce local retention and storage-size limits.

### Policy Storage

- Validate the policy schema and version.
- Reject malformed, unauthorized, expired, or unsupported policies.
- Store the latest accepted policy locally.
- Preserve the latest valid policy while offline.
- Ignore older policy versions after a newer version is accepted.

### Application and Game Enforcement

- Identify supported installed packages.
- Apply configured schedules using local device time rules.
- Apply supported application or game network restrictions through the local VPN design.
- Avoid affecting unrelated applications where technically possible.
- Report `ACTIVE`, `FAILED`, `UNSUPPORTED`, or `PENDING` accurately.
- Explain conflicts such as another active VPN.

### Domain Enforcement

- Apply a normalized domain-block policy.
- Support optional subdomain matching.
- Apply scheduled activation and removal.
- Avoid broad IP blocking that could affect unrelated domains.
- Report when protocol or DNS behavior prevents reliable enforcement.

### Health Reporting

- Usage-access permission state.
- VPN permission and service state.
- Last successful collection time.
- Last successful synchronization time.
- Queued-event count and oldest queued-event time.
- Active policy version.
- Last enforcement result and failure reason.
- Engine version and supported capability flags.

## 4. Event Types

The engine must expose versioned events for:

- Application activity.
- Application usage summary inputs.
- Domain observation.
- Possible external-link transition.
- Permission-state change.
- Monitoring-state change.
- VPN-state change.
- Synchronization health.
- Policy acknowledgement.
- Enforcement result.

Every event requires a stable identifier, device identifier, event time, schema version, and data-quality status.

## 5. Flutter Platform Contract

The versioned platform channel must support:

- Permission-state queries.
- VPN-consent requests.
- Monitoring start and stop.
- Monitoring and queue status.
- Synchronization retry.
- Policy and enforcement status.
- Capability discovery.
- Structured native error responses.
- Native status updates delivered to Flutter.

The engine must not depend on a specific Flutter screen structure.

## 6. Backend Contracts

- Authenticated batch activity ingestion.
- Authenticated batch domain-observation ingestion.
- Device-health updates.
- Versioned policy download.
- Policy receipt and enforcement acknowledgement.
- Device credential rejection and revocation response.

Local integration tests must use a mock server until the real backend is available.

## 7. Business Rules

- Collection runs only with required permission.
- Monitoring and VPN failure must never be hidden.
- A policy is acknowledged as active only after successful local application.
- Unsupported behavior must return `UNSUPPORTED`, not success.
- Possible Telegram activity must remain an unconfirmed signal.
- Inaccessible network content must not be labeled safe.
- The latest valid policy remains available during temporary offline periods.

## 8. Security and Privacy

- Use separate device credentials supplied by the pairing flow.
- Protect credentials with Android secure storage.
- Encrypt production network communication.
- Do not collect messages, keystrokes, credentials, screenshots, or decrypted HTTPS content.
- Minimize local event content and retention.
- Exclude sensitive data from logs and crash reports.
- Validate backend certificates and reject invalid policy payloads.

## 9. Performance and Reliability

- Measure battery impact during normal monitoring.
- Bound CPU, memory, network, and local storage use.
- Avoid tight polling loops.
- Recover after application restart, service restart, and device reboot where Android permits it.
- Handle network switching and temporary backend failure.
- Test across supported Android versions and representative device vendors.

## 10. Acceptance Criteria

The task is complete when:

- A physical Android device reports permitted application usage.
- Domain observation works for documented supported cases.
- Events survive offline periods and synchronize without duplicates.
- Flutter receives accurate permission, monitoring, queue, VPN, and policy states.
- A supported game restriction can be applied without blocking an unrelated test application.
- A supported domain policy is enforced in documented scenarios.
- Failed and unsupported policies never appear active.
- Possible Telegram activity is emitted only as an unconfirmed transition signal.
- Reboot, permission removal, VPN conflict, clock change, and network-loss tests pass.
- Battery, memory, network, privacy, and physical-device test results are documented.

## 11. Out of Scope

- Parent or child business UI.
- Backend account and policy-management logic.
- ML model training or inference.
- HTTPS decryption.
- Private-message or screen-content collection.
- Guaranteed detection of Facebook or Telegram group membership.
- iOS monitoring implementation.
