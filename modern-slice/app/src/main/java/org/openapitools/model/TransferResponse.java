package org.openapitools.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * TransferResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-02-22T04:29:49.650640493Z[Etc/UTC]", comments = "Generator version: 7.6.0")
public class TransferResponse implements Serializable {

  private static final long serialVersionUID = 1L;

  private String transactionId;

  /**
   * Gets or Sets status
   */
  public enum StatusEnum {
    SUCCESS("SUCCESS"),
    
    FAILED("FAILED");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static StatusEnum fromValue(String value) {
      for (StatusEnum b : StatusEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private StatusEnum status;

  private java.math.BigDecimal balanceAfter;

  public TransferResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public TransferResponse(String transactionId, StatusEnum status, java.math.BigDecimal balanceAfter) {
    this.transactionId = transactionId;
    this.status = status;
    this.balanceAfter = balanceAfter;
  }

  public TransferResponse transactionId(String transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  /**
   * Get transactionId
   * @return transactionId
  */
  @NotNull @Size(min = 1) 
  @JsonProperty("transactionId")
  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public TransferResponse status(StatusEnum status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
  */
  @NotNull 
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public TransferResponse balanceAfter(java.math.BigDecimal balanceAfter) {
    this.balanceAfter = balanceAfter;
    return this;
  }

  /**
   * Get balanceAfter
   * @return balanceAfter
  */
  @NotNull @Valid 
  @JsonProperty("balanceAfter")
  public java.math.BigDecimal getBalanceAfter() {
    return balanceAfter;
  }

  public void setBalanceAfter(java.math.BigDecimal balanceAfter) {
    this.balanceAfter = balanceAfter;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransferResponse transferResponse = (TransferResponse) o;
    return Objects.equals(this.transactionId, transferResponse.transactionId) &&
        Objects.equals(this.status, transferResponse.status) &&
        Objects.equals(this.balanceAfter, transferResponse.balanceAfter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionId, status, balanceAfter);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransferResponse {\n");
    sb.append("    transactionId: ").append(toIndentedString(transactionId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    balanceAfter: ").append(toIndentedString(balanceAfter)).append("\n");
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

