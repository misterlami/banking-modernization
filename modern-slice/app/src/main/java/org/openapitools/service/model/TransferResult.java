package org.openapitools.service.model;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransferResult(
    @NotBlank String transactionId,
    @NotNull Status status,
    @NotNull BigDecimal balanceAfter) {

  public enum Status {
    SUCCESS,
    FAILED
  }
}
