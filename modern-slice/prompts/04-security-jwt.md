Implement JWT authentication for the transfer endpoint only:

- POST /auth/login returns a signed JWT containing userId and role claims
- Transfer endpoint requires Bearer token
- Include token expiry and signature verification
- Return 401/403 using the standard error schema

Provide the minimal Spring Security configuration and verification steps.
