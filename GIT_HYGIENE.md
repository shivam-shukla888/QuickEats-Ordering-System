# 🌿 Git Hygiene & Best Practices Guide

This repository maintains strict software engineering git practices to ensure commit history remains clean, authentic, readable, and production-ready.

---

## 1. Commit Message Convention (Conventional Commits)
All future commits MUST follow the **Conventional Commits** specification:

`<type>(<scope>): <short description in present imperative tense>`

### Allowed Types:
- `feat`: A new user-facing feature or agent tool capability.
- `fix`: A bug fix or error handler correction.
- `docs`: Documentation updates (`README.md`, `BRAND.md`, OpenAPI annotations).
- `style`: Formatting, CSS tweaks, missing semi-colons (no code logic changes).
- `refactor`: Code restructuring without changing external API behaviors.
- `test`: Adding missing JUnit or Vitest tests.
- `chore`: Updating dependencies, build scripts, or Dockerfiles.

### Examples:
- ✅ `feat(agent): add RAG similarity score retrieval to recommendation endpoint`
- ✅ `fix(tracking): resolve hardcoded map recentering on custom user address`
- ✅ `docs(brand): document CraveCraft color palette and typography pairing`
- ❌ `fixed stuff`
- ❌ `added changes`

---

## 2. Commit Atomic Granularity
- **Small, Logical Commits**: Group related changes logically (e.g. one commit for backend entity + repo, one commit for controller + tests).
- **Never Commit Secrets**: Ensure `.env`, API keys (`GROQ_API_KEY`), and database passwords are in `.gitignore`. Use `.env.example` for templates.
- **Consistent Author Identity**: Verify identity before committing:
  ```bash
  git config user.name "Shivam Shukla"
  git config user.email "shivam-shukla888@users.noreply.github.com"
  ```
