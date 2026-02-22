package org.openapitools.controller;

import org.openapitools.api.DefaultApi;
import org.openapitools.mapper.AuthMapper;
import org.openapitools.mapper.TransferMapper;
import org.openapitools.model.LoginRequest;
import org.openapitools.model.LoginResponse;
import org.openapitools.model.TransferRequest;
import org.openapitools.model.TransferResponse;
import org.openapitools.service.AuthService;
import org.openapitools.service.TransferService;
import org.openapitools.service.model.AuthCommand;
import org.openapitools.service.model.AuthResult;
import org.openapitools.service.model.TransferCommand;
import org.openapitools.service.model.TransferResult;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

@RestController
@Validated
public class BankingController implements DefaultApi {

  private final AuthService authService;
  private final TransferService transferService;
  private final AuthMapper authMapper;
  private final TransferMapper transferMapper;

  public BankingController(
      AuthService authService,
      TransferService transferService,
      AuthMapper authMapper,
      TransferMapper transferMapper) {
    this.authService = authService;
    this.transferService = transferService;
    this.authMapper = authMapper;
    this.transferMapper = transferMapper;
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
    TransferCommand command = transferMapper.toCommand(accountId, transferRequest);
    TransferResult result = transferService.transfer(command);
    return ResponseEntity.ok(transferMapper.toResponse(result));
  }
}
