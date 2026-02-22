package org.openapitools.service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.openapitools.exception.AuthenticationFailedException;
import org.openapitools.service.model.JwtClaims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class JwtTokenService {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
  };

  private final byte[] secret;
  private final String issuer;

  public JwtTokenService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.issuer}") String issuer) {
    this.secret = secret.getBytes(StandardCharsets.UTF_8);
    this.issuer = issuer;
  }

  public JwtClaims authenticateCustomer(String authorizationHeader) {
    String token = extractBearerToken(authorizationHeader);
    Map<String, Object> payload = decodeAndValidate(token);
    String accountId = readString(payload, "accountId");
    String subject = readString(payload, "sub");
    List<String> roles = readStringList(payload, "roles");
    if (!roles.contains("CUSTOMER")) {
      throw new AuthenticationFailedException("Token is not authorized for CUSTOMER role");
    }
    return new JwtClaims(subject, accountId, roles);
  }

  private String extractBearerToken(String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      throw new AuthenticationFailedException("Missing or invalid Bearer token");
    }
    String token = authorizationHeader.substring("Bearer ".length()).trim();
    if (token.isEmpty()) {
      throw new AuthenticationFailedException("Missing or invalid Bearer token");
    }
    return token;
  }

  private Map<String, Object> decodeAndValidate(String token) {
    String[] parts = token.split("\\.");
    if (parts.length != 3) {
      throw new AuthenticationFailedException("Malformed JWT");
    }

    String signingInput = parts[0] + "." + parts[1];
    String expectedSignature = sign(signingInput);
    if (!MessageDigest.isEqual(
        expectedSignature.getBytes(StandardCharsets.UTF_8),
        parts[2].getBytes(StandardCharsets.UTF_8))) {
      throw new AuthenticationFailedException("JWT signature validation failed");
    }

    try {
      byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
      Map<String, Object> payload = OBJECT_MAPPER.readValue(payloadBytes, MAP_TYPE);
      validatePayload(payload);
      return payload;
    } catch (IllegalArgumentException ex) {
      throw new AuthenticationFailedException("Malformed JWT payload");
    } catch (Exception ex) {
      throw new AuthenticationFailedException("Unable to parse JWT payload");
    }
  }

  private void validatePayload(Map<String, Object> payload) {
    String tokenIssuer = readString(payload, "iss");
    if (!issuer.equals(tokenIssuer)) {
      throw new AuthenticationFailedException("JWT issuer is invalid");
    }

    long exp = readLong(payload, "exp");
    long now = Instant.now().getEpochSecond();
    if (now >= exp) {
      throw new AuthenticationFailedException("JWT has expired");
    }
  }

  private long readLong(Map<String, Object> payload, String claim) {
    Object value = payload.get(claim);
    if (value instanceof Number number) {
      return number.longValue();
    }
    throw new AuthenticationFailedException("JWT claim '" + claim + "' is missing or invalid");
  }

  private String readString(Map<String, Object> payload, String claim) {
    Object value = payload.get(claim);
    if (value instanceof String text && !text.isBlank()) {
      return text;
    }
    throw new AuthenticationFailedException("JWT claim '" + claim + "' is missing or invalid");
  }

  @SuppressWarnings("unchecked")
  private List<String> readStringList(Map<String, Object> payload, String claim) {
    Object value = payload.get(claim);
    if (!(value instanceof List<?> rawList) || rawList.isEmpty()) {
      throw new AuthenticationFailedException("JWT claim '" + claim + "' is missing or invalid");
    }

    try {
      return ((List<Object>) rawList).stream()
          .map(item -> {
            if (item instanceof String text && !text.isBlank()) {
              return text;
            }
            throw new AuthenticationFailedException("JWT claim '" + claim + "' is missing or invalid");
          })
          .toList();
    } catch (AuthenticationFailedException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new AuthenticationFailedException("JWT claim '" + claim + "' is missing or invalid");
    }
  }

  private String sign(String signingInput) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      return Base64.getUrlEncoder().withoutPadding()
          .encodeToString(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
      throw new IllegalStateException("Unable to validate JWT signature", ex);
    }
  }
}
