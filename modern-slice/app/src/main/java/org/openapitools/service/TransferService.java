package org.openapitools.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.function.Function;

import org.openapitools.exception.AccountNotFoundException;
import org.openapitools.exception.BusinessValidationException;
import org.openapitools.exception.InsufficientFundsException;
import org.openapitools.repository.TransferRepository;
import org.openapitools.repository.TransferRepository.LockedAccount;
import org.openapitools.service.model.TransferCommand;
import org.openapitools.service.model.TransferResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

@Service
@Validated
public class TransferService {

  private final TransferRepository transferRepository;

  public TransferService(TransferRepository transferRepository) {
    this.transferRepository = transferRepository;
  }

  @Transactional
  public TransferResult transfer(@Valid TransferCommand command) {
    if (command.sourceAccountId().equals(command.targetAccountId())) {
      throw new BusinessValidationException("Source and target account must be different");
    }

    if (command.amount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new BusinessValidationException("Transfer amount must be greater than 0");
    }

    BigDecimal normalizedAmount;
    int legacyAmount;
    try {
      normalizedAmount = command.amount().setScale(0, RoundingMode.UNNECESSARY);
      legacyAmount = normalizedAmount.intValueExact();
    } catch (ArithmeticException ex) {
      throw new BusinessValidationException("Transfer amount must be a whole number for the legacy schema");
    }

    Map<String, LockedAccount> lockedAccounts = transferRepository
        .lockAccountsForTransfer(command.sourceAccountId(), command.targetAccountId())
        .stream()
        .collect(java.util.stream.Collectors.toMap(LockedAccount::accountId, Function.identity()));

    LockedAccount sourceAccount = lockedAccounts.get(command.sourceAccountId());
    if (sourceAccount == null) {
      throw new AccountNotFoundException("Source account not found: " + command.sourceAccountId());
    }

    LockedAccount targetAccount = lockedAccounts.get(command.targetAccountId());
    if (targetAccount == null) {
      throw new AccountNotFoundException("Target account not found: " + command.targetAccountId());
    }

    if (sourceAccount.balance().compareTo(normalizedAmount) < 0) {
      throw new InsufficientFundsException("Insufficient funds in account: " + command.sourceAccountId());
    }

    BigDecimal sourceBalanceAfter = sourceAccount.balance().subtract(normalizedAmount);
    BigDecimal targetBalanceAfter = targetAccount.balance().add(normalizedAmount);

    transferRepository.updateBalance(command.sourceAccountId(), sourceBalanceAfter);
    transferRepository.updateBalance(command.targetAccountId(), targetBalanceAfter);

    String transactionId = transferRepository.insertTransferTransaction(
        command.sourceAccountId(),
        command.targetAccountId(),
        legacyAmount,
        command.reference());

    return new TransferResult(transactionId, TransferResult.Status.SUCCESS, sourceBalanceAfter);
  }
}
