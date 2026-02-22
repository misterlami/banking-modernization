package org.openapitools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Path;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class BankingModernizationApplicationTests {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final Path LEGACY_SCHEMA_PATH = Path.of("..", "..", "legacy-system", "schema_postgresql.sql")
      .toAbsolutePath()
      .normalize();

  @Container
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("legacydb")
      .withUsername("postgres")
      .withPassword("postgres");

  @DynamicPropertySource
  static void registerDatabaseProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("app.jwt.secret", () -> "test-jwt-secret");
    registry.add("app.jwt.issuer", () -> "banking-modernization");
    registry.add("app.jwt.ttl-seconds", () -> "3600");
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private DataSource dataSource;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() throws ScriptException {
    ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
        new FileSystemResource(LEGACY_SCHEMA_PATH.toFile()));
    populator.execute(dataSource);

    jdbcTemplate.update("DELETE FROM transaction");
    jdbcTemplate.update("DELETE FROM customer");

    insertCustomer("alice", "secret", "SRC-1001", 1000);
    insertCustomer("bob", "secret2", "DST-2002", 300);
  }

  @Test
  void loginAndTransferFlowUpdatesBalancesAndPersistsTransaction() throws Exception {
    String jwt = loginAndGetJwt("alice", "secret");

    mockMvc.perform(post("/accounts/SRC-1001/transfer")
            .header("Authorization", "Bearer " + jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "targetAccountId": "DST-2002",
                  "amount": 200,
                  "currency": "USD",
                  "reference": "invoice"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.transactionId").exists())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.balanceAfter").value(800));

    assertThat(getBalance("SRC-1001")).isEqualByComparingTo("800");
    assertThat(getBalance("DST-2002")).isEqualByComparingTo("500");

    Integer txCount = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM transaction WHERE fromactno = ? AND toactno = ? AND amount = ?",
        Integer.class,
        "SRC-1001",
        "DST-2002",
        200);
    assertThat(txCount).isEqualTo(1);
  }

  @Test
  void transferReturnsUnauthorizedWhenJwtIsMissing() throws Exception {
    mockMvc.perform(post("/accounts/SRC-1001/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "targetAccountId": "DST-2002",
                  "amount": 100,
                  "currency": "USD"
                }
                """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("AUTH_FAILED"));
  }

  @Test
  void transferReturnsForbiddenWhenTokenAccountDoesNotMatchPathAccount() throws Exception {
    String jwt = loginAndGetJwt("alice", "secret");

    mockMvc.perform(post("/accounts/DST-2002/transfer")
            .header("Authorization", "Bearer " + jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "targetAccountId": "SRC-1001",
                  "amount": 100,
                  "currency": "USD"
                }
                """))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("FORBIDDEN"));
  }

  private String loginAndGetJwt(String userid, String password) throws Exception {
    MvcResult loginResult = mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "userid": "%s",
                  "password": "%s"
                }
                """.formatted(userid, password)))
        .andExpect(status().isOk())
        .andReturn();
    JsonNode loginJson = OBJECT_MAPPER.readTree(loginResult.getResponse().getContentAsString());
    return loginJson.get("jwt").asText();
  }

  private void insertCustomer(String userid, String password, String accountId, int balance) {
    jdbcTemplate.update(
        """
            INSERT INTO customer (
              fname, lname, dob, userid, pword, actno, gender, balance,
              addressline1, addressline2, city, state, zip
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
        "Test",
        "User",
        "1990-01-01",
        userid,
        password,
        accountId,
        "NA",
        balance,
        "123 Main St",
        "Unit 1",
        "Chicago",
        "Illinois",
        60616);
  }

  private java.math.BigDecimal getBalance(String accountId) {
    return jdbcTemplate.queryForObject(
        "SELECT balance FROM customer WHERE actno = ?",
        java.math.BigDecimal.class,
        accountId);
  }
}
