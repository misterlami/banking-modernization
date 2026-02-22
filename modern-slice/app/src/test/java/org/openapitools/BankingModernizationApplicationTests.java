package org.openapitools;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.repository.CustomerAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BankingModernizationApplicationTests {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CustomerAuthRepository customerAuthRepository;

  @BeforeEach
  void setUp() {
    given(customerAuthRepository.findCustomerByUseridAndPassword(eq("alice"), eq("secret")))
        .willReturn(Optional.of(new CustomerAuthRepository.CustomerCredentials("alice", "1001")));
  }

  @Test
  void contextLoads() {
  }

  @Test
  void loginEndpointReturnsJwtForValidCredentials() throws Exception {
    mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "userid": "alice",
                  "password": "secret"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.jwt").exists())
        .andExpect(jsonPath("$.userId").value("alice"))
        .andExpect(jsonPath("$.roles[0]").value("CUSTOMER"));
  }

  @Test
  void transferEndpointReturnsSuccess() throws Exception {
    mockMvc.perform(post("/accounts/ACC-1/transfer")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "targetAccountId": "ACC-2",
                  "amount": 12.50,
                  "currency": "USD",
                  "reference": "invoice"
                }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.transactionId").exists())
        .andExpect(jsonPath("$.status").value("SUCCESS"))
        .andExpect(jsonPath("$.balanceAfter").value(987.5));
  }
}
