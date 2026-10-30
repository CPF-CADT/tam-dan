package com.tamdan.service.auth;

import com.tamdan.security.AuthenticatedDeviceContext;
import com.tamdan.security.AuthenticatedParentContext;

/**
 * Contract for resolving trusted identities and checking ownership or device state.
 */
public interface IdentityLookupService {
    AuthenticatedParentContext resolveParent(Long parentId);

    AuthenticatedDeviceContext resolveDevice(Long deviceId);

    boolean verifyChildOwnership(Long familyId, Long childId);

    boolean verifyActiveDevice(Long deviceId);
}
