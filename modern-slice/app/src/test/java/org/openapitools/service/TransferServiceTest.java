package org.openapitools.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.exception.AccountNotFoundException;
import org.openapitools.exception.BusinessValidationException;
import org.openapitools.exception.InsufficientFundsException;
import org.openapitools.repository.TransferRepository;
import org.openapitools.repository.TransferRepository.LockedAccount;
import org.openapitools.service.model.TransferCommand;
import org.openapitools.service.model.TransferResult;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

  @Mock
  private TransferRepository transferRepository;

  private TransferService transferService;

  @BeforeEach
  void setUp() {
    transferService = new TransferService(transferRepository);
  }

  @Test
  void transferThrowsInsufficientFundsWhenSourceBalanceIsTooLow() {
    given(transferRepository.lockAccountsForTransfer("SRC-1", "DST-1"))
        .willReturn(List.of(
            new LockedAccount("SRC-1", new BigDecimal("100")),
            new LockedAccount("DST-1", new BigDecimal("50"))));

    TransferCommand command = new TransferCommand("SRC-1", "DST-1", new BigDecimal("200"), "USD", "rent");

    assertThatThrownBy(() -> transferService.transfer(command))
        .isInstanceOf(InsufficientFundsException.class)
        .hasMessageContaining("Insufficient funds");
  }

  @Test
  void transferThrowsAccountNotFoundWhenTargetAccountDoesNotExist() {
    given(transferRepository.lockAccountsForTransfer("SRC-1", "MISSING"))
        .willReturn(List.of(new LockedAccount("SRC-1", new BigDecimal("500"))));

    TransferCommand command = new TransferCommand("SRC-1", "MISSING", new BigDecimal("200"), "USD", "rent");

    assertThatThrownBy(() -> transferService.transfer(command))
        .isInstanceOf(AccountNotFoundException.class)
        .hasMessageContaining("Target account not found");
  }

  @Test
  void transferThrowsBusinessValidationWhenAmountIsZeroOrNegative() {
    TransferCommand zeroAmount = new TransferCommand("SRC-1", "DST-1", BigDecimal.ZERO, "USD", "rent");
    TransferCommand negativeAmount = new TransferCommand("SRC-1", "DST-1", new BigDecimal("-1"), "USD", "rent");

    assertThatThrownBy(() -> transferService.transfer(zeroAmount))
        .isInstanceOf(BusinessValidationException.class)
        .hasMessageContaining("greater than 0");

    assertThatThrownBy(() -> transferService.transfer(negativeAmount))
        .isInstanceOf(BusinessValidationException.class)
        .hasMessageContaining("greater than 0");
  }

  @Test
  void transferReturnsSuccessAndPersistsBalanceChanges() {
    given(transferRepository.lockAccountsForTransfer("SRC-1", "DST-1"))
        .willReturn(List.of(
            new LockedAccount("SRC-1", new BigDecimal("1000")),
            new LockedAccount("DST-1", new BigDecimal("250"))));
    given(transferRepository.insertTransferTransaction(eq("SRC-1"), eq("DST-1"), eq(200), eq("rent")))
        .willReturn("42");

    TransferCommand command = new TransferCommand("SRC-1", "DST-1", new BigDecimal("200"), "USD", "rent");
    TransferResult result = transferService.transfer(command);

    assertThat(result.transactionId()).isEqualTo("42");
    assertThat(result.status()).isEqualTo(TransferResult.Status.SUCCESS);
    assertThat(result.balanceAfter()).isEqualByComparingTo("800");

    verify(transferRepository).updateBalance("SRC-1", new BigDecimal("800"));
    verify(transferRepository).updateBalance("DST-1", new BigDecimal("450"));
    verify(transferRepository).insertTransferTransaction("SRC-1", "DST-1", 200, "rent");
  }
}
