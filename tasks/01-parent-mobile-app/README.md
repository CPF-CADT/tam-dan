# PRD — Parent Mobile Application

## 1. Purpose

Build the Flutter application that parents use to manage child profiles and devices, review activity and safety information, receive alerts, and configure parental-control policies.

The application is a client of the Spring Boot backend. It does not communicate directly with the child device or ML services.

## 2. Primary User

An authenticated parent or guardian who owns the family account.

## 3. Goals

- Give the parent one mobile control center.
- Make activity and risks easy to understand.
- Let the parent create and review policies safely.
- Show the real state of monitoring and enforcement.
- Avoid presenting uncertain evidence as confirmed harmful activity.

## 4. Core Features

### Account Access

- Parent registration.
- Email or approved identity verification.
- Login and logout.
- Access-token refresh.
- Account recovery.
- Secure local token storage.
- Sign out from all parent sessions.

### Child Profiles

- List all child profiles owned by the family.
- Create a child profile with minimum required information.
- View and edit a child profile.
- Deactivate or request deletion of a child profile.
- Open the devices, reports, alerts, and policies for a selected child.

### Device Pairing and Management

- Request an expiring pairing code.
- Display the code and its expiry time.
- Show pairing success or failure.
- List paired devices.
- Display device name, platform, last synchronization, and monitoring state.
- Rename or revoke a device.
- Warn the parent before a device is revoked.

### Activity Dashboard

- Display daily and weekly usage totals.
- Display usage duration by application.
- Show the most-used applications.
- Compare current usage with a previous period.
- Show late-night usage and configured limit violations.
- Show when report data is incomplete or outdated.
- Filter reports by child, device, and date range.

### Website Safety

- List observed domains when available.
- Display risk category, confidence, status, and assessment time.
- Show `UNKNOWN` when evidence is insufficient.
- Display a simple explanation of why a result may be uncertain.
- Filter assessments by category, device, and date.

### Alerts

- List unread and reviewed alerts.
- Open alert details.
- Mark an alert as reviewed or dismissed.
- Filter alerts by type and child.
- Receive push notifications when enabled.
- Deep-link a notification to the correct alert.
- Display alerts for risk results, usage limits, late-night activity, permission removal, synchronization failure, and enforcement failure.

### Possible Telegram Group-Join Activity

- Display an alert when the backend reports possible Telegram invitation activity.
- Show the event time and originating application when available.
- Show the available evidence without exposing private content.
- Label the event as unconfirmed.
- Let the parent review or dismiss the event.
- Never display the event as proof that the child joined a group.

### Application Policies

- List supported applications reported by the child device.
- Configure a daily usage limit.
- Configure allowed or blocked time schedules.
- Enable, pause, update, or remove a policy.
- Display the current policy version.
- Display `PENDING`, `ACTIVE`, `FAILED`, or `UNSUPPORTED` status.

### Game Internet Restrictions

- Select a supported installed game.
- Request that its internet connection be restricted.
- Configure an optional schedule.
- Remove or pause the restriction.
- Show the device-confirmed enforcement result.
- Explain that the game may continue to work offline.

### Domain Policies

- Add a normalized domain to a block policy.
- Choose whether the policy includes subdomains.
- Configure an optional schedule.
- Edit, pause, or remove the policy.
- Show whether the child device confirmed enforcement.

### Device Health

- Display usage-access permission state.
- Display VPN permission and service state.
- Display the last successful synchronization.
- Display the latest policy version received by the device.
- Display monitoring gaps and failures.
- Provide appropriate parent guidance for each failure state.

## 5. Main Screens

- Welcome, registration, login, and recovery.
- Family and child selector.
- Child overview dashboard.
- Activity report.
- Website-risk history.
- Alert list and alert details.
- Device list, pairing, and device details.
- Application-policy editor.
- Game-restriction editor.
- Domain-policy editor.
- Parent account and notification settings.

## 6. Required Backend Contracts

- Authentication and token refresh.
- Child-profile create, read, update, and deactivate.
- Pairing-code creation and pairing status.
- Device list, health, rename, and revocation.
- Usage summaries and application list.
- Domain assessments and alert history.
- Alert review and dismissal.
- Policy create, update, list, pause, and delete.
- Policy-delivery and enforcement status.
- Push-notification device registration.

Development must use mock responses until each backend contract is available.

## 7. Business Rules

- A parent may access only resources owned by the authenticated family.
- A policy is `ACTIVE` only after confirmation from the target child device.
- Missing or stale data must be visible to the parent.
- Uncertain classifications must not be displayed as confirmed harm.
- Possible Telegram activity must remain an unconfirmed review signal.
- Destructive actions require clear confirmation.

## 8. Security and Privacy

- Store tokens using platform-secure storage.
- Do not log credentials, tokens, or sensitive child data.
- Clear sensitive local state after logout or device revocation.
- Require reauthentication for sensitive account operations where appropriate.
- Use HTTPS for production traffic.
- Follow accessible and child-safety-aware language throughout the interface.

## 9. Acceptance Criteria

The task is complete when:

- A parent can register, sign in, and recover access.
- A parent can create a child profile and pair a physical child device.
- Activity, website risks, alerts, and device health are understandable and correctly scoped.
- A parent can create application, game, and domain policies.
- The interface never reports a policy as active without device confirmation.
- Possible Telegram group-join activity is clearly marked as unconfirmed.
- Loading, empty, offline, stale-data, authorization, and server-error states are handled.
- Core flows pass widget, integration, accessibility, and physical-device tests.

## 10. Out of Scope

- Direct communication with the child device.
- ML training or inference inside the mobile application.
- Reading Facebook, Telegram, or private-message content.
- Web administration features.
- Child-device monitoring implementation.
