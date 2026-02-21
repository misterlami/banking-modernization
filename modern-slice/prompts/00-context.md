
You are my AI coding agent.

Goal: modernize one vertical slice of the legacy repo (Login + Transfer) using OpenAPI-first and Spring Boot 3.

Constraints: 8 hours total. Depth over breadth. Two flows only.
Repo rules: treat legacy code as reference only. Do not use or copy from any existing modernization folder.

Non-negotiables: contract is source of truth, endpoints must match OpenAPI, tests must pass, CI must be green, container-first.

Output structure must be under `modern-slice/`.

Whenever you generate code, also generate: tests, validation, error model, and minimal documentation.

Always propose a plan, then implement in small steps, and after each step provide verification commands.
