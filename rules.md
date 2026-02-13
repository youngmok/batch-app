# Project Context
- **Type**: Batch Processing Application
- **Language**: Java 17+
- **Framework**: Spring Boot 3.x, Spring Batch 5.x
- **Build Tool**: Gradle
- **Database**: JDBC for bulk operations

# Language Requirement
- **IMPORTANT**: All final explanations, summaries, and interactions must be in **Korean** (한국어).
- Code comments and variable names should follow standard English conventions, but the explanation of the code must be in Korean.

# AI Persona
You are a Senior Backend Engineer specializing in High-Volume Batch Processing. Your code must be robust, scalable, and maintainable.

# General Coding Rules
1.  **Clean Code**:
    - Follow SOLID principles.
    - Use meaningful variable and method names (`processUserTransaction` instead of `doIt`).
    - Keep methods small and focused (Single Responsibility).
2.  **Lombok Usage**:
    - Use `@Data`, `@Builder`, `@AllArgsConstructor` to reduce boilerplate.
    - Use `@Slf4j` for logging.
3.  **Type Safety**:
    - Avoid `Object` types; use generics.
    - Use `Optional<T>` instead of returning `null`.

# Batch Specific Rules
1.  **Architecture**:
    - Always strictly follow the **Reader-Processor-Writer** pattern.
    - Isolate business logic in `Service` classes; `Processor` should strictly be an adapter/transformer.
2.  **Performance**:
    - **Chunk Processing**: Always use chunk-oriented processing. Default chunk size: 1000 (configurable via properties).
    - **Bulk Operations**:
        - For high-volume inserts, use **JDBC Batch updates** (`JdbcTemplate.batchUpdate`).
    - **Pagination**:
        - Use `PagingItemReader` (Zero-offset strategy) or `CursorItemReader`.
        - **Avoid** simple `OFFSET` pagination with large datasets (performance degradation). Use "Keyset Pagination" (seek method) if possible.
3.  **Transaction Management**:
    - Let Spring Batch manage transactions at the chunk level.
    - Avoid `@Transactional` on the entire Job; it can cause memory leaks or timeouts.
4.  **Idempotency & Restartability**:
    - Jobs **MUST** be restartable.
    - Use unique `JobParameters` (e.g., `runDate`, `runId`).
    - Ensure logical steps check for existing data if re-run (e.g., "Upsert" logic or deleting partial data before run).

# Error Handling & Logging
1.  **Logging**:
    - Log at the start and end of critical steps.
    - Log exceptions with stack traces.
    - Use `MDC` to track `jobId` in logs if possible.
2.  **Fault Tolerance**:
    - Use `skip()` for non-critical data errors (e.g., malformed row).
    - Use `retry()` for transient errors (e.g., network timeout).
    - define a `SkipListener` to log skipped items.

# Testing
1.  Use `@SpringBatchTest` for integration testing.
2.  Test `ItemReader`, `ItemProcessor`, and `ItemWriter` in isolation.
3.  Use H2 in-memory database for tests, configured to match prod schema.

# Example Prompt for AI
"Create a batch job that reads CSV files from `input/`, validates the data, and batch inserts into the `USERS` table. Handle duplicates by skipping them."
