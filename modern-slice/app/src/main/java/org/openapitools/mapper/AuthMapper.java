package org.openapitools.mapper;

import org.openapitools.model.LoginRequest;
import org.openapitools.model.LoginResponse;
import org.openapitools.service.model.AuthCommand;
import org.openapitools.service.model.AuthResult;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

  public AuthCommand toCommand(LoginRequest request) {
    return new AuthCommand(request.getUserid(), request.getPassword());
  }

  public LoginResponse toResponse(AuthResult result) {
    return new LoginResponse(result.jwt(), result.userId(), result.roles());
  }
}
