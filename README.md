# Bee 🐝

An 🐣 _incubator_ for `be`.

Bee 🐝 is a Task/Action/Workflow engine & tool (CLI), which can be a build tool - but not only.

This is a part of and may eventually get integrated into [enola](https://github.com/enola-dev/enola) (or not; TBD).

## Building

Requires Java 21+:

```bash
mvn clean compile
```

## Null Safety

This project uses [JSpecify](https://jspecify.dev/) annotations for null safety. To check for null safety issues:

```bash
./check-nulls.sh
```

See [NULL_CHECKER.md](NULL_CHECKER.md) for more details on the null safety system.
