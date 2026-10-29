# Task PRD Index

Each software component has an independent product requirements document:

1. [Parent Mobile Application](01-parent-mobile-app/README.md)
2. [Child Mobile Application](02-child-mobile-app/README.md)
3. [Android Monitoring and Enforcement Engine](03-android-monitoring-engine/README.md)
4. [Identity and Device Backend](04-identity-device-backend/README.md)
5. [Safety and Policy Backend](05-safety-policy-backend/README.md)
6. [Integration Hardening and OWASP Security Remediation](06-integration-hardening-security/README.md)
7. [Remaining Product Completion](07-remaining-product-completion/README.md)

ML dataset preparation and model training are separate workstreams and are not included in these five PRDs.

The integration-hardening PRD is a P0 merge gate for the seven blockers returned by Integration Review. It must be completed after the feature work and before merge or deployment.

PRD 7 tracks the remaining end-to-end product work for the Parent Mobile, Child Mobile, Android, Identity/Device Backend, and Safety/Policy Backend PRDs.
