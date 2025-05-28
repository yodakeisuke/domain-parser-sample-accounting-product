# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
This is a Kotlin JVM project implementing an accounting domain_accounting using Domain-Driven Design principles with a functional programming paradigm.

## Build & Development Commands

```bash
# Build the project
./gradlew build

# Clean build  
./gradlew clean build

# Compile only (faster, skips tests)
./gradlew compileKotlin

# Run tests
./gradlew test

# Run specific test class
./gradlew test --tests "workflow.JournalEntryRegistrationTest"

# Run with test output
./gradlew test --info
```

## Architecture

### Domain Structure
The domain_accounting is organized in three layers under `src/main/kotlin/domain_accounting/`:

- **command/**: Event-sourced aggregates that process commands and produce events
- **read/**: Read models providing views/projections for queries  
- **term/**: Domain vocabulary including operations, resources, policies, and values

### Workflow Layer
- **Workflows**: Pure functions that orchestrate domain_accounting commands with infrastructure effects
- **Dependency injection via functions**: Effects are passed as function parameters, not interfaces
- **Request/Response pattern**: Uses data classes like `RegisterJournalEntryRequest`

### Effect Layer
- **No interfaces**: Effects are modeled as functions with clear signatures
- **Result type**: All effects return `Result<T, E>` for explicit error handling
- **In-memory implementations**: For testing without external dependencies

### Core Patterns

1. **Functional Programming**: 
   - Pure functions with immutable data
   - No classes for business logic - use functions
   - Higher-order functions for dependency injection
   
2. **Error Handling**: 
   - Result type pattern using `kotlin-result` library
   - No exceptions in domain_accounting logic
   - Explicit error types (e.g., `JournalEntryRegistrationError`)
   - Option 1: Using binding - most readable
   - 

3. **Event-Driven**: 
   - Commands produce events (Registered, Corrected, Approved)
   - Events are stored, not state
   - Aggregates reconstruct state from events

4. **Business Rules**: 
   - Each rule is a single function
   - Rules compose monadically/applicatively
   - Example: `requireMinimumTwoLines` andThen `requireBalancedEntry`

### Key Domain Concepts

- **Accounting Domain**: Journal entries (仕訳登録) and P/L statements (損益計算書)
- **Core Types**: 
  - `AccountingAmount`: Sealed interface with Unsigned (with DebitCredit) and Signed variants
  - `Account`: Created via `Account.from()` returning Result
  - `JournalLine`: Combines Account, Amount, and description
  - `JournalEntry`: Event-sourced aggregate with register/correct/approve commands
- **Value Classes**: `NonEmptyString`, `PositiveBigDecimal`, `ID<T>` for type safety
- **Sign Normalization**: Converts DebitCredit to +1/-1 based on AccountType
- **Domain Logic**: `JournalLine.aggregateByAccount` for account-based aggregation

### Workflow Implementation Pattern
```kotlin
fun registerJournalEntry(
    deriveEvent: (JournalEntry) -> Result<JournalEntry, String>,  // Effect returns what was written
    request: RegisterJournalEntryRequest                           // Request data
): Result<JournalEntry.Registered, JournalEntryRegistrationError> {
    return JournalEntry.register(request.header, request.lines)
        .mapError { ValidationFailed(it.reason) }
        .andThen { event ->
            deriveEvent(event)
                .mapError { SaveFailed(it) }
                .map { it as JournalEntry.Registered }
        }
}
```

## Coding Standards

1. Express domain_accounting knowledge declaratively through types and structure
2. Prefer functions over classes for business logic
3. Use function parameters instead of interfaces for effects
4. No comments - let types and function names convey meaning
5. One business rule = one function
6. Use Result type for all operations that can fail
7. Place domain_accounting logic in appropriate locations (e.g., companion objects)
8. Always ensure builds are green before completing work

## Testing Patterns

- Use `assertTrue(result.isOk)` for Result assertions
- Test both success and failure paths
- Use function references for effect injection: `::saveJournalEvent`
- Clear test data with `@BeforeEach` when using in-memory stores