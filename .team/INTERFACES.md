# Interface Contracts: Focus Mode App

**Created**: 2026-04-10
**Last Updated**: 2026-04-10

> These contracts define how modules communicate. Every module MUST implement its side of each contract. Teammates reference this file to ensure compatibility.

## Shared Types

```
{Define shared types/interfaces that multiple modules use.}
```

## API Contracts

### {Module A} -> {Module B}

**Endpoint/Function**: {name}
**Input**: {types}
**Output**: {types}
**Error cases**: {error types}
**Notes**: {any important details}

---

### {Module B} -> {Module C}

**Endpoint/Function**: {name}
**Input**: {types}
**Output**: {types}
**Error cases**: {error types}
**Notes**: {any important details}

## Event Contracts

| Event Name | Producer | Consumer(s) | Payload |
| ---------- | -------- | ----------- | ------- |
| {event} | {module} | {modules} | {shape} |

## Database Contracts

| Table/Collection | Owning Module | Read Access |
| ---------------- | ------------- | ----------- |
| {name} | {module} | {which modules can read} |
