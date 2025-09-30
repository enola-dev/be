# Agent Instructions

## Java Version Target

This project targets **Java 25** and uses features introduced in Java 19+, including:

- Virtual Threads (`Executors.newVirtualThreadPerTaskExecutor()`)
- `Future.state()` API for structured task status management

**Do not make changes for backwards compatibility with older Java versions.**

## Testing

Tests are located in the `test/` directory and can be run using:

```bash
./run-tests.sh
```

Note: Tests require Java 25 to compile and run due to the use of modern Java features.

## Code Style

- Follow existing code patterns in the repository
- Maintain minimal, focused changes
- Keep test coverage comprehensive but concise
