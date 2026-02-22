package org.openapitools.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthCommand(
    @NotBlank @Size(max = 128) String userId,
    @NotBlank @Size(max = 256) String password) {
}
