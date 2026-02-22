package org.openapitools.service.model;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record AuthResult(
    @NotBlank String jwt,
    @NotBlank String userId,
    @NotEmpty List<@NotBlank String> roles) {
}
