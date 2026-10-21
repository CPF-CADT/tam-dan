package com.tamdan.dto.response;

import java.util.Set;

public record ParentAccountResponse(Long id, String login, String email, Boolean activated, Set<String> authorities) {}
