
Generate integration tests derived from OpenAPI examples for both endpoints.

Also generate unit tests for TransferService covering: insufficient funds, invalid target account, negative amount, and happy path.

Requirements: tests must run in CI, use Testcontainers for Postgres if feasible; otherwise H2 with schema compatibility notes.

Output verification commands and expected results.
