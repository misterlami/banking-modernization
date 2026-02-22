package org.openapitools.mapper;

import org.openapitools.model.TransferRequest;
import org.openapitools.model.TransferResponse;
import org.openapitools.service.model.TransferCommand;
import org.openapitools.service.model.TransferResult;
import org.springframework.stereotype.Component;

@Component
public class TransferMapper {

  public TransferCommand toCommand(String accountId, TransferRequest request) {
    return new TransferCommand(
        accountId,
        request.getTargetAccountId(),
        request.getAmount(),
        request.getCurrency(),
        request.getReference());
  }

  public TransferResponse toResponse(TransferResult result) {
    return new TransferResponse(
        result.transactionId(),
        mapStatus(result.status()),
        result.balanceAfter());
  }

  private TransferResponse.StatusEnum mapStatus(TransferResult.Status status) {
    return switch (status) {
      case SUCCESS -> TransferResponse.StatusEnum.SUCCESS;
      case FAILED -> TransferResponse.StatusEnum.FAILED;
    };
  }
}
