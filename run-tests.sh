#!/bin/bash
# Test runner script for the BE project

set -e

echo "==================================="
echo "Building and Testing BE Project"
echo "==================================="
echo ""

# Clean and create build directories
echo "Cleaning build directories..."
rm -rf /tmp/be-classes /tmp/be-test-classes
mkdir -p /tmp/be-classes /tmp/be-test-classes

# Compile source code
echo "Compiling source code..."
javac -d /tmp/be-classes \
    src/dev/enola/be/task/*.java \
    src/dev/enola/be/task/demo/*.java \
    src/dev/enola/be/exec/*.java

echo "✓ Source code compiled successfully"
echo ""

# Compile test code
echo "Compiling test code..."
javac -cp /tmp/be-classes -d /tmp/be-test-classes \
    test/dev/enola/be/task/*.java \
    test/dev/enola/be/task/demo/*.java

echo "✓ Test code compiled successfully"
echo ""

# Run tests
echo "Running tests..."
echo ""

java -ea -cp /tmp/be-classes:/tmp/be-test-classes dev.enola.be.task.StatusTest
echo ""

java -ea -cp /tmp/be-classes:/tmp/be-test-classes dev.enola.be.task.TaskTest
echo ""

java -ea -cp /tmp/be-classes:/tmp/be-test-classes dev.enola.be.task.TaskExecutorTest
echo ""

java -ea -cp /tmp/be-classes:/tmp/be-test-classes dev.enola.be.task.demo.LongIncrementingTaskTest
echo ""

echo "==================================="
echo "All tests passed successfully! ✓"
echo "==================================="
