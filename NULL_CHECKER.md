# Null Safety Checker for Bee 🐝

This document describes the simple null safety checking system added to the Bee project.

## Overview

The Bee project uses [JSpecify](https://jspecify.dev/) annotations to document and enforce null safety at compile time. This is a minimal, lightweight approach that provides immediate feedback about potential null pointer issues without requiring complex tooling.

## What's Included

1. **JSpecify Annotations**: The `org.jspecify:jspecify` dependency provides `@Nullable` and `@NonNull` annotations
2. **Annotated Code**: Key areas of the codebase have been annotated to document nullability
3. **Simple Checker Script**: A `check-nulls.sh` script that validates null safety

## How It Works

### Annotations Used

- **`@Nullable`**: Indicates that a parameter, return value, or field can be `null`
- **`@NonNull`** (implicit): By default, all types are considered non-null unless marked with `@Nullable`

### Examples in the Codebase

#### Task Input Parameters
```java
// Task input can be nullable
protected Task(@Nullable I input) {
    this.input = input;
}
```

#### Optional Record Fields
```java
// Duration is optional in LongIncrementingTask
record Input(long max, @Nullable Duration sleep) { }
```

#### Null Check Before Use
```java
// Properly checking null before dereferencing
Duration sleep = input.sleep;
if (sleep != null) {
    Thread.sleep(sleep.toMillis());
}
```

## Running the Null Checker

### Prerequisites

- Java 21 or later
- Maven 3.x

### Basic Usage

```bash
./check-nulls.sh
```

This script will:
1. Verify Java and Maven are available
2. Compile the project with all warnings enabled
3. Report any null safety issues

### Manual Checking

You can also run the checks manually using Maven:

```bash
export JAVA_HOME=/path/to/java21
mvn clean compile
```

## Adding Null Safety to New Code

When writing new code:

1. **Mark nullable parameters and return values**:
   ```java
   public void processTask(@Nullable Task task) {
       if (task != null) {
           // Safe to use task here
       }
   }
   ```

2. **Check for null before dereferencing**:
   ```java
   @Nullable String value = getValue();
   if (value != null) {
       int length = value.length(); // Safe
   }
   ```

3. **Use defensive coding**:
   ```java
   public Task getTask(UUID id) {
       var task = tasks.get(id);
       if (task == null) {
           throw new IllegalArgumentException("No such task: " + id);
       }
       return task; // Non-null guaranteed
   }
   ```

## Current Annotated Areas

The following areas have been annotated for null safety:

- **`Task.java`**: Input parameter and return value marked as `@Nullable`
- **`TaskExecutor.java`**: Proper null checking in `get()` method
- **`ExecTask.java`**: Optional `cwd` and `env` parameters marked as `@Nullable`
- **`LongIncrementingTask.java`**: Optional `sleep` parameter marked as `@Nullable`

## Future Enhancements

While this is the simplest possible null checker, future enhancements could include:

1. **Static Analysis Tools**: Integration with tools like:
   - [NullAway](https://github.com/uber/NullAway) for fast, practical null checking
   - [EISOP Checker Framework](https://eisop.github.io/cf/) for comprehensive type checking
   - [SpotBugs](https://spotbugs.github.io/) for additional bug detection

2. **IDE Integration**: Leveraging IntelliJ IDEA or Eclipse null analysis features

3. **CI/CD Integration**: Running null checks as part of the continuous integration pipeline

4. **Library Models**: Adding nullability annotations for external dependencies

## References

- [JSpecify](https://jspecify.dev/) - Standard nullness annotations for Java
- [Issue #845](https://github.com/enola-dev/enola/issues/845) - Original discussion on null safety for Enola.dev projects
- [LastNPE.org](http://www.lastnpe.org) - Historical project on null safety in Java

## License

This null safety checker and documentation are part of the Bee project and follow the same Apache 2.0 license.
