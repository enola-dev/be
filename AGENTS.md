# Agent Instructions

## Java Version Target

This project targets **Java 25** and uses features introduced in Java 19+, including:

- Virtual Threads (`Executors.newVirtualThreadPerTaskExecutor()`)
- `Future.state()` API for structured task status management

**Do not make changes for backwards compatibility with older Java versions.**

## Testing

Tests can be run using:

```bash
./test.sh
```

Note: Tests require Java 25 to compile and run due to the use of modern Java features.

## Code Style

- Follow existing code patterns in the repository
- Maintain minimal, focused changes
- Keep test coverage comprehensive but concise
- Use `var` for local variables wherever possible to reduce verbosity
- Avoid `System.out.println` in test code - tests should be silent unless they fail
- Inline simple method calls and arguments unless they are reused
- Avoid explicit null checks - rely on NullPointerExceptions to catch null issues
- Use full file extensions (e.g., `*.yaml` not `*.yml`, `*.markdown` not `*.md`) - it's 2025!
