package com.tamdan.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenResponse(@JsonProperty("id_token") String idToken) {}
