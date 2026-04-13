# DB Engineer Conventions

- **Table naming**: {e.g., plural snake_case -- users, order_items}
- **Column naming**: {e.g., snake_case -- created_at, user_id}
- **Primary keys**: {e.g., UUID v4, column named "id"}
- **Foreign keys**: {e.g., {referenced_table_singular}_id -- user_id, order_id}
- **Timestamps**: {e.g., every table has created_at and updated_at}
- **Soft deletes**: {e.g., deleted_at column, nullable}
- **Indexes**: {e.g., on all foreign keys and frequently queried columns}
- **Migrations**: {e.g., sequential numbered, always reversible, descriptive names}
- **Seed data**: {e.g., separate files per table, idempotent}
