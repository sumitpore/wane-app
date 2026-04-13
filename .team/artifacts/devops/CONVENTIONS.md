# DevOps Conventions

- **Environment naming**: {e.g., development, staging, production}
- **Config management**: {e.g., .env files per environment, never committed}
- **Docker**: {e.g., multi-stage builds, non-root user, .dockerignore}
- **CI/CD stages**: {e.g., lint -> test -> build -> deploy}
- **Branch strategy**: {e.g., main + feature branches, PRs required}
- **Health checks**: {e.g., /health endpoint, returns 200 with status JSON}
- **Logging infrastructure**: {e.g., stdout, collected by log aggregator}
