package org.openapitools.service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.openapitools.repository.CustomerAuthRepository;
import org.openapitools.exception.AuthenticationFailedException;
import org.openapitools.repository.CustomerAuthRepository.CustomerCredentials;
import org.openapitools.service.model.AuthCommand;
import org.openapitools.service.model.AuthResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;

@Service
@Validated
public class AuthService {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final CustomerAuthRepository customerAuthRepository;
  private final byte[] secret;
  private final String issuer;
  private final long ttlSeconds;

  public AuthService(
      CustomerAuthRepository customerAuthRepository,
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.issuer}") String issuer,
      @Value("${app.jwt.ttl-seconds}") long ttlSeconds) {
    this.customerAuthRepository = customerAuthRepository;
    this.secret = secret.getBytes(StandardCharsets.UTF_8);
    this.issuer = issuer;
    this.ttlSeconds = ttlSeconds;
  }

  public AuthResult authenticate(@Valid AuthCommand command) {
    CustomerCredentials customer = customerAuthRepository
        .findCustomerByUseridAndPassword(command.userId(), command.password())
        .orElseThrow(() -> new AuthenticationFailedException("Invalid userid or password"));

    List<String> roles = List.of("CUSTOMER");
    String token = createToken(customer.userid(), customer.actno(), roles);
    return new AuthResult(token, customer.userid(), roles);
  }

  private String createToken(String userId, String accountId, List<String> roles) {
    Instant now = Instant.now();
    long issuedAt = now.getEpochSecond();
    long expiresAt = now.plusSeconds(ttlSeconds).getEpochSecond();

    String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("iss", issuer);
    payload.put("sub", userId);
    payload.put("iat", issuedAt);
    payload.put("exp", expiresAt);
    payload.put("roles", roles);
    payload.put("accountId", accountId);

    String encodedHeader = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
    String encodedPayload = base64UrlEncode(json(payload).getBytes(StandardCharsets.UTF_8));
    String signingInput = encodedHeader + "." + encodedPayload;
    String signature = sign(signingInput);
    return signingInput + "." + signature;
  }

  private String json(Map<String, Object> payload) {
    try {
      return OBJECT_MAPPER.writeValueAsString(payload);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Unable to serialize JWT payload", ex);
    }
  }

  private String sign(String signingInput) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      return base64UrlEncode(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
      throw new IllegalStateException("Unable to sign JWT", ex);
    }
  }

  private String base64UrlEncode(byte[] bytes) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
