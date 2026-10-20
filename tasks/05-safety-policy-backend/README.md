# PRD — Safety and Policy Backend

## 1. Purpose

Build the Spring Boot module that owns child activity, domain observations, reports, rule-based activity analysis, risk assessments, alerts, parent policies, policy delivery, and device enforcement acknowledgements.

The module consumes trusted parent, family, child, and device identity from the Identity and Device module. It integrates with externally developed ML inference services but does not train models.

## 2. Goals

- Store child activity reliably and without duplicates.
- Produce understandable parent reports.
- Convert reviewed safety evidence into appropriate alerts.
- Manage application, game, and domain policies.
- Report the real delivery and enforcement state.
- Integrate ML results through stable, versioned contracts.

## 3. Core Features

### Activity Ingestion

- Accept authenticated batches from active child devices.
- Validate device identity and child association.
- Validate schema version, identifiers, timestamps, and allowed event types.
- Reject malformed or unauthorized records.
- Use stable event identifiers for idempotency.
- Return per-batch acceptance and rejection information.
- Preserve data-quality and monitoring-gap indicators.

### Domain Observation Ingestion

- Accept normalized domain observations from authenticated devices.
- Validate domain format and event time.
- Deduplicate repeated events according to documented rules.
- Store the source device and observation context.
- Queue classification only when a valid cached result is unavailable.

### Usage Reports

- Produce daily and weekly usage totals.
- Calculate usage by application and application category where available.
- Identify most-used applications.
- Compare current and previous periods.
- Mark incomplete periods caused by missing monitoring or synchronization.
- Support queries by family, child, device, and date range.

### Rule-Based Activity Analysis

- Detect parent-configured usage-limit violations.
- Detect configured late-night activity.
- Identify repeated policy violations.
- Identify large changes compared with an established previous period.
- Count repeated observations by risk category.
- Store the exact rule version and evidence.
- Avoid behavioral or mental-health diagnoses.

### Possible Telegram Group-Join Activity

- Accept possible external-link transition signals from the Android engine.
- Validate the application sequence and event timing.
- Add supported domain evidence when available.
- Store an evidence summary without private content.
- Generate a review event according to parent notification settings.
- Label the result as unconfirmed.
- Allow the parent to review or dismiss the event.
- Never claim confirmed Telegram group membership.

### Domain Classification Integration

- Submit a normalized domain to the external domain ML API.
- Validate and store category, confidence, status, model version, and taxonomy version.
- Cache results by normalized input and model version.
- Handle timeouts, unavailable models, invalid responses, and low confidence.
- Store `UNKNOWN` when evidence is insufficient.

### Public-Content Classification Integration

- Request public-content retrieval only under approved product rules.
- Protect retrieval against private-network and unsafe redirect targets.
- Apply content-type, response-size, redirect, and timeout limits.
- Sanitize approved public text before inference.
- Submit text to the external content ML API.
- Store labels, confidence values, language, status, and model version.
- Treat inaccessible content as unavailable evidence, never as `SAFE`.

### Risk Decision

- Preserve each model result separately.
- Combine model results and reviewed lists through versioned decision rules.
- Keep conflicting or low-confidence evidence visible.
- Produce `UNKNOWN` when the evidence cannot support a category.
- Store the decision version and evidence summary.
- Avoid presenting a prediction as proof of harmful intent.

### Parent Alerts

- Create alerts from approved risk and activity rules.
- Support severity and alert type.
- Avoid duplicate alerts for the same configured condition.
- Track unread, reviewed, dismissed, and resolved states.
- Respect parent notification preferences.
- Queue push notifications without placing sensitive details in notification text.
- Create operational alerts for permission, synchronization, and enforcement failures.

### Application Policies

- Create daily limits and allowed or blocked schedules.
- Target a specific child device and application package.
- Validate time zones and schedule rules.
- Support enable, pause, update, and removal.
- Create an immutable version for every change.

### Game Internet Policies

- Target a supported installed game package.
- Enable or disable internet restriction.
- Support an optional schedule.
- Record when the device reports unsupported behavior or VPN conflict.
- Explain that offline game behavior may remain available.

### Domain Policies

- Store normalized blocked domains.
- Support optional subdomain matching.
- Support temporary and recurring schedules.
- Validate domain syntax and prevent overly broad accidental policies.
- Record device enforcement limitations.

### Policy Delivery and Acknowledgement

- Provide the latest authorized policy version to the target device.
- Prevent delivery to revoked or unrelated devices.
- Record download and receipt status.
- Record enforcement result and failure reason.
- Preserve history for every policy version.
- Report `ACTIVE` only after successful device acknowledgement.
- Return `PENDING`, `FAILED`, or `UNSUPPORTED` accurately.

### Parent Queries

- Activity summaries and trends.
- Domain observations and assessments.
- Alert list, details, review, and dismissal.
- Installed or observed application list where available.
- Policy list and current state.
- Device monitoring and enforcement summary.

## 4. Core Data Entities

- `ActivityEvent`
- `AppUsageSummary`
- `DomainObservation`
- `ContentEvidence`
- `ModelPrediction`
- `RiskAssessment`
- `ActivityRuleResult`
- `TransitionReviewEvent`
- `ParentAlert`
- `ApplicationPolicy`
- `GameNetworkPolicy`
- `DomainPolicy`
- `DevicePolicyVersion`
- `PolicyAcknowledgement`

Database migrations must be versioned and tested.

## 5. External Contracts

### Identity and Device Module

- Authenticated parent and family context.
- Authenticated active-device context.
- Child-profile and device ownership verification.
- Device capability and revocation events.

### Domain ML Service

- Versioned domain-classification request.
- Category, confidence, status, taxonomy version, and model version response.
- Liveness, readiness, timeout, and error behavior.

### Content ML Service

- Versioned sanitized-text request.
- Labels, confidence values, language, status, taxonomy version, and model version response.
- Liveness, readiness, timeout, and error behavior.

### Mobile Clients

- Activity and domain batch-ingestion schemas.
- Report, assessment, and alert response schemas.
- Policy creation and status schemas.
- Device policy download and acknowledgement schemas.

All unavailable dependencies require mocks or stubs for independent development.

## 6. Business Rules

- Parent queries must be scoped to the authenticated family.
- Device ingestion must be scoped to the authenticated active device.
- Event ingestion must be idempotent.
- Missing monitoring data must be visible in reports.
- ML failure or unavailable content must never become `SAFE` automatically.
- Possible Telegram activity remains an unconfirmed review event.
- Every policy change creates a new immutable version.
- A policy becomes `ACTIVE` only after successful device acknowledgement.
- Rule and model versions must remain traceable for historical results.

## 7. Security and Privacy

- Enforce object-level authorization on every protected operation.
- Validate all ingested data and external ML responses.
- Rate-limit ingestion, classification, and policy endpoints.
- Protect public-content retrieval against SSRF and oversized responses.
- Use HTTPS in production.
- Exclude credentials, private content, and unnecessary child data from logs.
- Apply retention and deletion rules to activity, content evidence, and alerts.
- Keep push-notification payloads minimal.

## 8. Reliability and Operations

- Support safe retries for ingestion and external ML calls.
- Use caching without hiding model-version changes.
- Process expensive classification asynchronously where appropriate.
- Expose liveness and readiness health endpoints.
- Measure ingestion delay, queue depth, report freshness, classification latency, errors, alert volume, and policy acknowledgement.
- Support database backup and tested restoration.
- Support rollback to a previous approved decision or model configuration.

## 9. Acceptance Criteria

The task is complete when:

- Authenticated devices can upload activity and domain batches without duplicates.
- Parents can query correct, family-scoped daily and weekly reports.
- Rule-based activity results are explainable and versioned.
- Domain and content ML mocks and real endpoints satisfy the same contract.
- Risk assessments preserve source predictions and uncertainty.
- Alerts can be created, reviewed, dismissed, and notified.
- Possible Telegram activity is never represented as confirmed membership.
- Application, game, and domain policies are versioned and delivered correctly.
- The parent sees active enforcement only after device confirmation.
- Authorization, SSRF, idempotency, retry, retention, migration, and integration tests pass.

## 10. Out of Scope

- Parent and child mobile UI.
- Android monitoring and VPN implementation.
- Parent authentication, pairing, and device credential issuance.
- ML dataset creation, training, or model selection.
- Private-message collection or HTTPS decryption.
- Confirmed Facebook or Telegram group-membership tracking.
