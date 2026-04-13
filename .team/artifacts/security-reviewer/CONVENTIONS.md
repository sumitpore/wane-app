# Security Reviewer Conventions

- **Input validation**: {e.g., validate and sanitize all user input at entry points}
- **Output encoding**: {e.g., context-aware encoding for HTML, URL, JS}
- **Secrets management**: {e.g., environment variables, rotated quarterly}
- **Dependency policy**: {e.g., no known critical CVEs, audit monthly}
- **Headers**: {e.g., CSP, HSTS, X-Frame-Options on all responses}
