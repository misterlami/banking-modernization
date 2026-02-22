package org.openapitools.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.openapitools.exception.ForbiddenException;
import org.openapitools.api.DefaultApi;
import org.openapitools.mapper.AuthMapper;
import org.openapitools.mapper.TransferMapper;
import org.openapitools.model.LoginRequest;
import org.openapitools.model.LoginResponse;
import org.openapitools.model.TransferRequest;
import org.openapitools.model.TransferResponse;
import org.openapitools.service.AuthService;
import org.openapitools.service.JwtTokenService;
import org.openapitools.service.TransferService;
import org.openapitools.service.model.AuthCommand;
import org.openapitools.service.model.AuthResult;
import org.openapitools.service.model.JwtClaims;
import org.openapitools.service.model.TransferCommand;
import org.openapitools.service.model.TransferResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Validated
public class BankingController implements DefaultApi {

  private final AuthService authService;
  private final TransferService transferService;
  private final JwtTokenService jwtTokenService;
  private final AuthMapper authMapper;
  private final TransferMapper transferMapper;

  public BankingController(
      AuthService authService,
      TransferService transferService,
      JwtTokenService jwtTokenService,
      AuthMapper authMapper,
      TransferMapper transferMapper) {
    this.authService = authService;
    this.transferService = transferService;
    this.jwtTokenService = jwtTokenService;
    this.authMapper = authMapper;
    this.transferMapper = transferMapper;
  }

  @GetMapping("/")
  public ResponseEntity<Map<String, Object>> rootStatus() {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("status", "UP");
    payload.put("service", "modern-slice-api");
    payload.put("message", "Modern slice API is running");
    payload.put("timestamp", Instant.now());
    payload.put("loginEndpoint", "POST /auth/login");
    payload.put("transferEndpoint", "POST /accounts/{accountId}/transfer");
    payload.put("swaggerUi", "http://localhost:8081");
    return ResponseEntity.ok(payload);
  }

  @Override
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
    AuthCommand command = authMapper.toCommand(loginRequest);
    AuthResult result = authService.authenticate(command);
    return ResponseEntity.ok(authMapper.toResponse(result));
  }

  @Override
  public ResponseEntity<TransferResponse> transferFunds(
      @Size(min = 1, max = 64) @PathVariable("accountId") String accountId,
      @Valid @RequestBody TransferRequest transferRequest) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
        .getRequest();
    if (request == null) {
      throw new IllegalStateException("No HTTP request context available");
    }
    String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    JwtClaims claims = jwtTokenService.authenticateCustomer(authorizationHeader);
    if (!accountId.equals(claims.accountId())) {
      throw new ForbiddenException("Token account does not match requested account");
    }

    TransferCommand command = transferMapper.toCommand(accountId, transferRequest);
    TransferResult result = transferService.transfer(command);
    return ResponseEntity.ok(transferMapper.toResponse(result));
  }
}
