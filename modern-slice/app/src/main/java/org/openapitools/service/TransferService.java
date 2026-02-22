package org.openapitools.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.openapitools.exception.BusinessValidationException;
import org.openapitools.service.model.TransferCommand;
import org.openapitools.service.model.TransferResult;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

@Service
@Validated
public class TransferService {

  private static final BigDecimal STARTING_BALANCE = new BigDecimal("1000.00");

  public TransferResult transfer(@Valid TransferCommand command) {
    if (command.sourceAccountId().equals(command.targetAccountId())) {
      throw new BusinessValidationException("Source and target account must be different");
    }

    BigDecimal rawBalance = STARTING_BALANCE.subtract(command.amount());
    BigDecimal balanceAfter = rawBalance.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

    return new TransferResult(UUID.randomUUID().toString(), TransferResult.Status.SUCCESS, balanceAfter);
  }
}
