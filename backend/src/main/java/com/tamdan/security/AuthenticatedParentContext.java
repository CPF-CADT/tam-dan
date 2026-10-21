package com.tamdan.security;

/**
 * Trusted identity context for an authenticated parent.
 */
public record AuthenticatedParentContext(Long parentId, Long familyId) {}
