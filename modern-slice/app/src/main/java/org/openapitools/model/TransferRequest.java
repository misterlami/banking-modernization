package org.openapitools.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.Serializable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * TransferRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-02-22T04:29:49.650640493Z[Etc/UTC]", comments = "Generator version: 7.6.0")
public class TransferRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  private String targetAccountId;

  private java.math.BigDecimal amount;

  private String currency;

  private String reference;

  public TransferRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public TransferRequest(String targetAccountId, java.math.BigDecimal amount, String currency) {
    this.targetAccountId = targetAccountId;
    this.amount = amount;
    this.currency = currency;
  }

  public TransferRequest targetAccountId(String targetAccountId) {
    this.targetAccountId = targetAccountId;
    return this;
  }

  /**
   * Get targetAccountId
   * @return targetAccountId
  */
  @NotNull @Size(min = 1, max = 64) 
  @JsonProperty("targetAccountId")
  public String getTargetAccountId() {
    return targetAccountId;
  }

  public void setTargetAccountId(String targetAccountId) {
    this.targetAccountId = targetAccountId;
  }

  public TransferRequest amount(java.math.BigDecimal amount) {
    this.amount = amount;
    return this;
  }

  /**
   * Get amount
   * @return amount
  */
  @NotNull @Valid 
  @JsonProperty("amount")
  public java.math.BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(java.math.BigDecimal amount) {
    this.amount = amount;
  }

  public TransferRequest currency(String currency) {
    this.currency = currency;
    return this;
  }

  /**
   * Get currency
   * @return currency
  */
  @NotNull @Pattern(regexp = "^[A-Z]{3}$") @Size(min = 3, max = 3) 
  @JsonProperty("currency")
  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public TransferRequest reference(String reference) {
    this.reference = reference;
    return this;
  }

  /**
   * Get reference
   * @return reference
  */
  @Size(min = 1, max = 140) 
  @JsonProperty("reference")
  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransferRequest transferRequest = (TransferRequest) o;
    return Objects.equals(this.targetAccountId, transferRequest.targetAccountId) &&
        Objects.equals(this.amount, transferRequest.amount) &&
        Objects.equals(this.currency, transferRequest.currency) &&
        Objects.equals(this.reference, transferRequest.reference);
  }

  @Override
  public int hashCode() {
    return Objects.hash(targetAccountId, amount, currency, reference);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransferRequest {\n");
    sb.append("    targetAccountId: ").append(toIndentedString(targetAccountId)).append("\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
    sb.append("    reference: ").append(toIndentedString(reference)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

