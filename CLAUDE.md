# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
This is a Kotlin JVM project implementing an accounting domain using Domain-Driven Design principles with a functional programming paradigm.

## Build & Development Commands

```bash
# Build the project
./gradlew build

# Clean build
./gradlew clean build
```

## Architecture

### Domain Structure
The domain is organized in three layers under `src/main/kotlin/domain/`:

- **command/**: Command models (aggregates) that process user instructions and produce events
- **read/**: Read models providing views/snapshots for queries  
- **term/**: Domain vocabulary including operations, resources, policies, and values

### Core Patterns

1. **Functional Programming**: Pure functions + immutable data, no class-based thinking
2. **Error Handling**: Result type pattern using `kotlin-result` library, no exceptions
3. **Event-Driven**: Commands produce events, aggregates are bundles of commands/events
4. **Business Rules**: Each rule is a single function, composed monadically or applicatively

### Key Domain Concepts

- **Accounting Domain**: Journal entries (仕訳登録) and P/L statements (損益計算書)
- **Core Types**: Account, Amount, DebitCredit, SignConvention
- **Aggregates**: JournalEntry handles journal registration commands

## Coding Standards

1. Express domain knowledge declaratively through types and structure, not procedurally
2. Minimize public API surface - only expose what's necessary
3. No comments - let types and structure convey meaning
4. Always ensure builds are green before completing work
5. One business rule = one function
6. Use function composition (monadic/applicative) for complex rules
7. Never introduce incidental complexity

## Testing

Because this is a sample project, test code isn’t required.