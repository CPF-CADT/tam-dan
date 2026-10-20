# Tam Dan — Child Online Safety and Parental Control

## 1. Overview

Tam Dan helps parents understand and manage a child's online activity from a mobile phone.

The platform allows parents to:

- See how the child uses installed applications.
- Review websites and online content that may be unsafe.
- Receive alerts about risky or unusual activity.
- Detect possible Telegram group-join activity for parent verification.
- Set application time limits and schedules.
- Restrict supported online games and websites.
- Confirm whether monitoring and restrictions are working.

Tam Dan is designed for transparent parental supervision. The child application clearly shows that monitoring is active and which permissions are being used.

## 2. Product Applications

### Parent Mobile Application

The parent application is the main control center. It allows parents to:

- Register and sign in securely.
- Create and manage child profiles.
- Pair and remove child devices.
- View daily and weekly application usage.
- See the child's most-used applications.
- Review website-risk classifications.
- Receive usage, website-risk, and device-status alerts.
- Review possible Telegram group-join activity.
- Set application schedules and daily limits.
- Restrict supported online games.
- Block supported websites or domains.
- View whether monitoring and restrictions are active.
- Review or dismiss alerts.

The parent application is built with Flutter and can support Android and iOS phones.

### Child Mobile Application

The child application provides a clear and transparent device experience. It allows the child-device user to:

- Pair the device with an authorized parent.
- See what information the application collects.
- Review and grant required permissions.
- View monitoring and synchronization status.
- View active schedules and restrictions.
- See whether a restriction is active or has failed.
- Continue basic monitoring while temporarily offline.

The child application is built with Flutter. Android is the primary child-device platform.

## 3. Core Product Features

### Application-Usage Monitoring

- Track which applications are used.
- Calculate usage duration by application.
- Show daily and weekly screen-time summaries.
- Identify the most frequently used applications.
- Detect late-night usage.
- Detect when a parent-configured usage limit is exceeded.
- Compare current usage with previous periods.

### Website and Domain Safety

- Observe supported website-domain activity.
- Classify domains into safety-risk categories.
- Analyze approved public webpage text when available.
- Show the classification category and confidence.
- Alert parents about repeated or high-risk results.
- Return `UNKNOWN` when there is not enough evidence.
- Allow parents to block supported domains.

### Possible Telegram Group-Join Activity

Tam Dan can identify activity that may indicate the child opened a Telegram group invitation from Facebook or a web browser.

The feature allows the parent to:

- Receive a possible group-join activity alert.
- See when the activity happened.
- See the application from which the Telegram activity started when available.
- Review or dismiss the alert.
- Decide whether to discuss or verify the activity with the child.

This feature provides a review signal. It cannot confirm that the child completed the group join, identify every group, or read private group content.

### Parent Alerts

Parents may receive alerts for:

- High-risk website classifications.
- Repeated visits to risky content categories.
- Excessive application usage.
- Late-night activity.
- Possible Telegram group-join activity.
- Removed monitoring permissions.
- Failed synchronization.
- Failed or unsupported restrictions.

### Application Schedules and Limits

- Set daily time limits for selected applications.
- Create allowed or blocked schedules.
- Apply different rules for selected applications.
- Temporarily pause or resume a restriction.
- Show whether the child device received the latest rule.

### Online-Game Internet Restriction

- Select a supported game installed on the child device.
- Request an internet restriction for that game.
- Keep unrelated applications connected where technically possible.
- Show whether enforcement is pending, active, unsupported, or failed.
- Report `ACTIVE` only after the child device confirms enforcement.

A restricted game may continue to provide offline functionality.

### Website Blocking

- Block a supported domain.
- Optionally include its subdomains.
- Apply a temporary or recurring schedule.
- Remove the restriction from the parent application.
- Show whether the child device successfully applied the restriction.

### Offline Operation and Device Health

- Store activity temporarily when the child device is offline.
- Synchronize stored activity when connectivity returns.
- Prevent duplicate activity records.
- Preserve the latest accepted restriction policy.
- Show the last successful synchronization time.
- Report missing permissions or inactive monitoring.
- Report policy and enforcement failures.

## 4. Five Main Development Tasks

The project is divided into five software components. Machine-learning training is separate from these tasks.

Detailed PRDs are available in the [`tasks/`](tasks/README.md) folder.

### Task 1 — Parent Mobile Application

**Technology:** Flutter

- Parent account and authentication screens.
- Child-profile management.
- Device pairing and removal.
- Application-usage reports.
- Website-risk results and alerts.
- Telegram activity review alerts.
- Application schedules and restrictions.
- Game and website controls.
- Device and policy status.
- Parent notifications.

### Task 2 — Child Mobile Application

**Technology:** Flutter

- Device-pairing flow.
- Monitoring and privacy explanation.
- Permission setup guidance.
- Monitoring and synchronization status.
- Active restriction information.
- Error and recovery guidance.
- Communication with the Android monitoring engine.

### Task 3 — Android Monitoring and Enforcement Engine

**Technology:** Kotlin and Android platform APIs

- Application-usage collection.
- Supported domain observation.
- Detection signals for possible Telegram group-join activity.
- Secure offline event storage.
- Background synchronization support.
- Application and domain restriction support.
- Local VPN management.
- Permission and enforcement health reporting.

### Task 4 — Identity and Device Backend

**Technology:** Spring Boot and PostgreSQL

- Parent registration and authentication.
- Family and child-profile ownership.
- Secure, expiring device-pairing codes.
- Child-device registration.
- Separate device authentication.
- Device credential rotation and revocation.
- Authorization and consent records.
- Security audit history.

### Task 5 — Safety and Policy Backend

**Technology:** Spring Boot, PostgreSQL, and optional Redis

- Activity and domain-data collection.
- Daily and weekly report generation.
- Rule-based activity analysis.
- Website-risk classification integration.
- Parent-alert generation.
- Telegram activity review events.
- Application, game, and website policies.
- Policy versioning and delivery.
- Device confirmation and failure tracking.

The two backend tasks can be developed as separate modules inside one Spring Boot application.

## 5. Machine-Learning Opportunities

Machine-learning development is managed separately from the five software tasks.

### Domain Classification

The domain model estimates the safety category of a website domain.

- Uses approved domain-level features.
- Provides a fast result when webpage text is unavailable.
- Returns a category, confidence, and model version.
- Returns `UNKNOWN` when the domain does not provide enough evidence.

### Public-Content Text Classification

The content model analyzes approved text from publicly accessible webpages.

- Uses page titles, headings, metadata, and visible text.
- Supports Khmer, English, and mixed-language evaluation when data permits.
- May return multiple relevant risk categories.
- Does not read private messages or decrypt the child's HTTPS traffic.

### Activity-Risk Analysis

An optional activity model may analyze aggregated usage patterns.

- Excessive application usage.
- Late-night activity.
- Sudden changes in usage.
- Repeated exposure to risky content categories.
- Repeated policy violations.

This model requires sufficient reviewed historical data. Until then, Tam Dan uses clear statistical rules. Activity results must not be presented as addiction, intent, personality, or mental-health diagnoses.

### Risk Categories

- `SAFE`
- `ADULT`
- `GAMBLING`
- `SCAM_OR_FRAUD`
- `VIOLENCE`
- `DRUGS`
- `SELF_HARM`
- `OTHER_RISK`
- `UNKNOWN`

## 6. System Architecture

```text
┌────────────────────────────┐
│ Parent Flutter Application │
│ Reports, alerts, controls  │
└──────────────┬─────────────┘
               │
               ▼
┌──────────────────────────────────────────────┐
│ Spring Boot Application                      │
│                                              │
│ ┌───────────────────┐  ┌──────────────────┐ │
│ │ Identity & Device │  │ Safety & Policy  │ │
│ └───────────────────┘  └────────┬─────────┘ │
└──────────────┬──────────────────┼───────────┘
               │                  │
               │                  ├──► Domain Classification
               │                  ├──► Content Classification
               │                  └──► Optional Activity Analysis
               │
               ▼
┌────────────────────────────┐
│ Child Flutter Application  │
│ Pairing, consent, status   │
└──────────────┬─────────────┘
               │
               ▼
┌────────────────────────────┐
│ Kotlin Android Engine      │
│ Monitoring and enforcement│
└────────────────────────────┘
```

## 7. Main Data Flow

### Activity and Safety Flow

```text
Child device collects permitted activity
    → Backend stores and analyzes the activity
    → ML services classify supported website evidence
    → Backend creates reports and alerts
    → Parent reviews the information in the mobile app
```

### Parent-Control Flow

```text
Parent creates a restriction
    → Backend sends the latest policy to the child device
    → Android engine attempts enforcement
    → Child device reports the result
    → Parent sees the confirmed status
```

## 8. Important Boundaries

- The parent application may run on Android or iOS.
- Android is the primary child-device platform.
- Future iOS child support requires a separate Swift implementation and will provide fewer network-control features.
- Monitoring permissions and VPN approval must be granted on the child device.
- The system cannot reliably confirm Facebook or Telegram group membership.
- The system does not collect private messages, passwords, keystrokes, or decrypted HTTPS content.
- Website classifications and activity alerts are safety indicators for parent review, not proof of harmful behavior.
