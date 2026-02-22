package org.openapitools.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import org.openapitools.exception.AuthenticationFailedException;
import org.openapitools.service.model.AuthCommand;
import org.openapitools.service.model.AuthResult;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

@Service
@Validated
public class AuthService {

  public AuthResult authenticate(@Valid AuthCommand command) {
    if ("locked".equalsIgnoreCase(command.userId()) || "invalid".equals(command.password())) {
      throw new AuthenticationFailedException("Invalid userid or password");
    }

    String tokenPayload = command.userId() + ":" + Instant.now().toEpochMilli();
    String token = Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(tokenPayload.getBytes(StandardCharsets.UTF_8));

    List<String> roles = "admin".equalsIgnoreCase(command.userId())
        ? List.of("USER", "ADMIN")
        : List.of("USER");

    return new AuthResult("stub." + token + ".sig", command.userId(), roles);
  }
}
