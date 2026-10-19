# AI-Assisted Child Online Safety & Digital Wellbeing

## Project Overview

This project will deliver a parental-awareness platform that helps parents understand their children's online activity, identify potentially harmful websites, monitor application usage, and manage internet access for selected online games.

The system will consist of an Android child application and a web dashboard for parents. The Android application will collect permitted activity and network signals, while the parent dashboard will present usage summaries, website-risk results, alerts, and parental controls.

The first release will use rule-based website detection. Its architecture will allow a supervised machine-learning classifier to be added later without redesigning the main applications.

## Final MVP Deliverables

The completed five-week MVP will provide:

- Parent registration and secure authentication.
- Secure pairing between a parent account and an Android device.
- Transparent permission and monitoring-status information on the child device.
- Android application-usage tracking.
- Collection of domain signals where Android and network conditions allow it.
- Rule-based website classification and risk alerts.
- Daily and weekly activity reports for parents.
- Parent-configured internet restrictions for selected games.
- Device confirmation of monitoring and policy-enforcement status.
- A stable risk-classification interface for future machine-learning integration.

## Users

### Parent

The parent will be able to:

- Register and sign in.
- Generate a device-pairing code.
- View and manage paired devices.
- Review application usage and observed domains.
- View website-risk results and alert history.
- Configure internet restrictions for selected games.
- Review daily and weekly activity summaries.

### Child Device

The child device will:

- Pair with an authorized parent account.
- Explain and request the required Android permissions.
- Collect permitted application-usage and domain signals.
- Synchronize collected events with the backend.
- Receive and store parental policies.
- Apply supported network restrictions locally.
- Display service health, permission state, and last synchronization time.

## Main Features

### 1. Device Pairing

The parent will generate a secure, expiring pairing code. The child enters the code to register the device and receive separate device credentials.

### 2. Application-Usage Monitoring

The Android application will use `UsageStatsManager` to collect permitted information about application usage and gaming duration.

### 3. Website Monitoring

The Android VPN service will observe supported domain or network signals. Monitoring coverage may vary by application and protocol, so the system will report only what it can technically observe.

### 4. Website-Risk Detection

The MVP will classify websites using:

- Known-domain blocklists.
- Keyword matching against publicly retrievable website content.
- Cached results from previous classifications.
- An `UNKNOWN` result when there is insufficient evidence.

Supported categories:

- `SAFE`
- `GAMBLING`
- `ADULT`
- `SCAM`
- `UNKNOWN`

Rule-based classifications are preliminary safety signals, not proof that a website is harmful.

### 5. Parent Dashboard

The web dashboard will show:

- Paired devices and their current status.
- Daily and weekly application usage.
- Observed domains and classification results.
- Risk-alert history.
- Configured game-restriction policies.
- Monitoring, synchronization, and enforcement failures.

### 6. Game Internet Restrictions

Parents will be able to select supported game applications and configure internet restrictions. The child application will receive the policy and use local VPN filtering to restrict the selected application's network access.

A policy will not be shown as successfully enforced until the child device confirms it.

## System Architecture

```text
┌──────────────────────────┐       ┌──────────────────────────┐
│ Android Child App        │       │ Parent Web Dashboard     │
│ Flutter + Kotlin         │       │ Next.js                  │
│                          │       │                          │
│ • Usage monitoring       │       │ • Devices                │
│ • VPN service            │       │ • Usage reports          │
│ • Policy enforcement     │       │ • Risks and alerts       │
└────────────┬─────────────┘       └────────────┬─────────────┘
             │                                  │
             └──────────────┬───────────────────┘
                            │
                 ┌──────────▼───────────┐
                 │ Spring Boot REST API │
                 │                      │
                 │ • Authentication     │
                 │ • Device pairing     │
                 │ • Activity storage   │
                 │ • Policies           │
                 │ • Risks and alerts   │
                 └───────┬────────┬─────┘
                         │        │
              ┌──────────▼──┐  ┌──▼───────────────────┐
              │ PostgreSQL  │  │ FastAPI Risk Service │
              │             │  │ Retrieval + rules    │
              └─────────────┘  └──────────┬───────────┘
                                          │
                               Future ML classifier
```

The Spring Boot backend will be implemented as a modular monolith rather than separate Java microservices.

Planned backend modules:

```text
com.childsafety
├── auth
├── user
├── device
├── pairing
├── activity
├── monitoring
├── risk
├── policy
├── notification
└── common
    ├── security
    ├── exception
    └── config
```

## Core Data Entities

| Entity | Responsibility |
|---|---|
| `User` | Parent account |
| `ChildDevice` | Device identity, owner, and status |
| `PairingCode` | Secure and expiring device pairing |
| `DeviceCredential` | Authentication for paired devices |
| `AppUsageRecord` | Application usage duration by date |
| `DomainObservation` | Observed domain and timestamp |
| `RiskAssessment` | Category, evidence, method, and status |
| `RiskAlert` | Parent-facing safety alert |
| `GamePolicy` | Package-level internet restriction |
| `DevicePolicyVersion` | Policy delivery and acknowledgement |

## Main Workflows

### Device Setup

1. The parent registers and signs in.
2. The parent generates an expiring pairing code.
3. The child enters the code on the Android device.
4. The backend registers the device and issues device credentials.
5. The child application guides the user through Android permissions.
6. The device begins monitoring and synchronization.

### Website Monitoring

1. The Android application observes a domain where technically possible.
2. The backend checks for an existing classification.
3. An unknown domain may be sent to the risk service.
4. The risk service retrieves public content and applies classification rules.
5. The result is stored and displayed in the parent dashboard.
6. A risk alert is created when appropriate.

### Game Restriction

1. The parent selects a game application.
2. The parent enables an internet-restriction policy.
3. The backend stores and versions the policy.
4. The child device downloads and stores the update.
5. The VPN service applies the restriction locally.
6. The child device reports whether enforcement succeeded.

## Security, Privacy, and Reliability

### Security

- Parent authentication will use JWT-based sessions.
- Paired devices will use separate device credentials, not the parent's JWT.
- Pairing codes will expire and be usable only for authorized pairing.
- Every parent request will verify resource ownership.
- Every device request will verify device identity.
- Production communication will require HTTPS.

### Privacy

- Monitoring will be visible to the child-device user.
- The system will collect only information required by the MVP.
- Private messages will not be collected.
- HTTPS traffic will not be decrypted.
- Collected activity will have a limited retention period.

### Reliability

- Activity events will be uploaded in batches.
- Risk classifications will be cached.
- Received policies will be stored locally on the device.
- The system will report VPN, permission, synchronization, and enforcement failures.
- Inaccessible website content will never be automatically classified as safe.

## Five-Week Development Plan

### Week 1 — Android Feasibility and Project Foundation

**Objective:** Prove that Android usage monitoring and selective network filtering are technically feasible.

- [ ] Create the Flutter child application and Kotlin platform-channel bridge.
- [ ] Implement the usage-access permission flow.
- [ ] Retrieve application usage with `UsageStatsManager`.
- [ ] Create a basic Android `VpnService` prototype.
- [ ] Inspect which domain and network signals are observable.
- [ ] Test routing or dropping traffic for one selected application.
- [ ] Initialize the backend, web application, database, and project structure.

**Completion target:** A physical Android device reports application usage, and the VPN prototype demonstrates selective filtering for a test application.

### Week 2 — Backend Authentication, Pairing, and Ingestion

**Objective:** Establish secure communication between parent accounts, child devices, and the backend.

- [ ] Implement parent registration and login.
- [ ] Add JWT authentication and authorization.
- [ ] Implement secure, expiring pairing codes.
- [ ] Register child devices and issue device credentials.
- [ ] Create PostgreSQL entities and migrations.
- [ ] Implement batch application-usage ingestion.
- [ ] Implement batch domain-observation ingestion.
- [ ] Connect the Android application to the backend.

**Completion target:** A parent can pair a physical Android device, and the device can securely upload usage and domain events.

### Week 3 — Parent Dashboard and Policy Management

**Objective:** Give parents visibility into device activity and control over game policies.

- [ ] Build parent registration and login pages.
- [ ] Build device-pairing and device-management screens.
- [ ] Create the activity overview dashboard.
- [ ] Display daily and weekly application usage.
- [ ] Display observed domains and device status.
- [ ] Implement game-policy creation and updates.
- [ ] Synchronize versioned policies to Android.
- [ ] Store policies locally and report acknowledgements.

**Completion target:** A parent can view synchronized activity and configure a policy that reaches the paired device.

### Week 4 — Website-Risk Detection and Alerts

**Objective:** Add rule-based website analysis without requiring a trained model.

- [ ] Create the FastAPI classification interface.
- [ ] Retrieve accessible public website content safely.
- [ ] Add known-domain blocklists.
- [ ] Add keyword-based classification rules.
- [ ] Return `UNKNOWN` when evidence is insufficient.
- [ ] Cache classification results.
- [ ] Integrate the risk service with Spring Boot.
- [ ] Store risk assessments and generate alerts.
- [ ] Display risk results and alert history on the dashboard.

**Completion target:** Observed domains can be classified by rules, stored by the backend, and displayed to parents with appropriate alerts.

### Week 5 — Integration, Testing, and MVP Demonstration

**Objective:** Stabilize the complete workflow and demonstrate the final MVP on a physical Android device.

- [ ] Test registration, pairing, monitoring, and synchronization end to end.
- [ ] Test website classification and alert delivery.
- [ ] Test policy updates and acknowledgements.
- [ ] Verify restriction of one supported game.
- [ ] Confirm that unrelated applications remain connected.
- [ ] Add monitoring and enforcement health indicators.
- [ ] Review authorization and device-identity checks.
- [ ] Review privacy, data retention, battery use, and failure handling.
- [ ] Package local services with Docker Compose.
- [ ] Prepare the final MVP demonstration and documentation.

**Completion target:** All major workflows operate together, and the MVP meets the acceptance criteria below.

## Scope-Control Rule

Selective per-application VPN filtering is the highest technical risk. If reliable filtering is not achieved by the end of Week 2, the team will continue delivering usage monitoring, backend integration, the dashboard, and risk detection while treating game restriction as experimental.

The system must never report a restriction as active unless the Android device confirms enforcement.

## MVP Acceptance Criteria

The MVP is complete when:

- A parent can register and pair a physical Android device.
- The Android application can collect application-usage statistics.
- The Android application can report technically observable domains.
- The backend can securely store and retrieve device activity.
- Rule-based website classification works.
- Risk assessments and alerts appear in the parent dashboard.
- The parent can configure a game-restriction policy.
- One supported game can be restricted while unrelated applications remain connected.
- The system clearly reports inactive monitoring and failed enforcement.
- All major workflows can be demonstrated on a physical Android device.

## Out of Scope for the Initial MVP

- Telegram group monitoring.
- Analysis of individual social-media posts or videos.
- HTTPS decryption.
- Collection of private messages.
- Prediction of child behavior or mental-health conditions.
- iOS support.
- Production-grade anti-tampering controls.
- A trained machine-learning model.

## Development After the MVP

### Phase 2 — Machine-Learning Integration

Prepare a labeled dataset, train and compare supervised text classifiers, evaluate them on unseen websites, and integrate the selected model through the existing FastAPI interface.

### Phase 3 — Khmer and Multilingual Classification

Improve detection for Khmer, English, and mixed-language websites. Compare word-based, character-based, and multilingual approaches.

### Phase 4 — Experimental Telegram Research

Investigate whether transparent and consented Android accessibility observations can provide useful public group metadata. This work will be added only if its technical coverage and privacy impact are acceptable.

### Phase 5 — Academic Evaluation

Compare rule-based and machine-learning detection, measure Android monitoring feasibility, evaluate whether alerts improve parental awareness, and document the results for possible academic publication.

