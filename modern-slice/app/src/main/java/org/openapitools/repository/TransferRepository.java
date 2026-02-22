package org.openapitools.repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TransferRepository {

  private final JdbcTemplate jdbcTemplate;

  public TransferRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<LockedAccount> lockAccountsForTransfer(String sourceAccountId, String targetAccountId) {
    String sql = """
        SELECT actno, balance
        FROM customer
        WHERE actno IN (?, ?)
        ORDER BY actno
        FOR UPDATE
        """;
    return jdbcTemplate.query(
        sql,
        (resultSet, rowNum) -> new LockedAccount(
            resultSet.getString("actno"),
            resultSet.getBigDecimal("balance")),
        sourceAccountId,
        targetAccountId);
  }

  public void updateBalance(String accountId, BigDecimal newBalance) {
    jdbcTemplate.update(
        "UPDATE customer SET balance = ? WHERE actno = ?",
        newBalance,
        accountId);
  }

  public String insertTransferTransaction(
      String sourceAccountId,
      String targetAccountId,
      int amount,
      String reference) {
    String remark = (reference == null || reference.isBlank()) ? "Funds transferred successfully" : truncate(reference, 40);
    String sql = """
        INSERT INTO transaction (fromactno, toactno, trandate, trandesc, transtatus, remark, amount, amountaction)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        RETURNING tranid
        """;
    Integer transactionId = jdbcTemplate.queryForObject(
        sql,
        Integer.class,
        sourceAccountId,
        targetAccountId,
        Timestamp.from(Instant.now()),
        "Funds Transfer Within the Bank",
        "pass",
        remark,
        amount,
        "debit");

    if (transactionId == null) {
      throw new IllegalStateException("Transfer transaction insert did not return tranid");
    }
    return transactionId.toString();
  }

  public record LockedAccount(String accountId, BigDecimal balance) {
  }

  private String truncate(String value, int maxLength) {
    if (value.length() <= maxLength) {
      return value;
    }
    return value.substring(0, maxLength);
  }
}
