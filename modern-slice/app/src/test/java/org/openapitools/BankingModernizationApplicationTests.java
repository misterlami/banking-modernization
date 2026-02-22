package org.openapitools;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BankingModernizationApplicationTests {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void contextLoads() {
  }

  @Test
  void loginEndpointReturnsStubToken() throws Exception {
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
        .andExpect(jsonPath("$.roles[0]").value("USER"));
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
