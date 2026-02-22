package org.openapitools.service.model;

import java.util.List;

public record JwtClaims(
    String subject,
    String accountId,
    List<String> roles) {
}
