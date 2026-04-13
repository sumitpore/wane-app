# Data Engineer Conventions

- **Event naming**: {e.g., object_action -- page_viewed, button_clicked, form_submitted}
- **Event properties**: {e.g., snake_case, always include timestamp and user_id}
- **Pipeline naming**: {e.g., {source}_to_{destination}_{frequency}}
- **Data formats**: {e.g., timestamps in ISO 8601 UTC, currency in cents}
- **Idempotency**: {e.g., all pipelines must be safely re-runnable}
