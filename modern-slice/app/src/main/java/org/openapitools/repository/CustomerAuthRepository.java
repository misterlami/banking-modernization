package org.openapitools.repository;

import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerAuthRepository {

  private final JdbcTemplate jdbcTemplate;

  public CustomerAuthRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public Optional<CustomerCredentials> findCustomerByUseridAndPassword(String userid, String password) {
    String sql = "SELECT userid, actno FROM customer WHERE userid = ? AND pword = ? LIMIT 1";
    return jdbcTemplate.query(
            sql,
            (resultSet, rowNum) -> {
              Object accountId = resultSet.getObject("actno");
              return new CustomerCredentials(
                  resultSet.getString("userid"),
                  accountId == null ? null : accountId.toString());
            },
            userid,
            password)
        .stream()
        .findFirst();
  }

  public record CustomerCredentials(String userid, String actno) {
  }
}
