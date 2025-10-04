#!/usr/bin/env bash
set -euo pipefail

rm -rf .build/classes .build/test-classes
mkdir -p .build/classes .build/test-classes

find src -name '*.java' ! -name '*Test.java' ! -name 'Test*.java' -print0 \
  | xargs -0 javac -d .build/classes

find src \( -name '*Test.java' -o -name 'Test*.java' \) -print0 \
  | xargs -0 javac -cp .build/classes -d .build/test-classes

java -ea -cp .build/classes:.build/test-classes dev.enola.be.task.StatusTest
java -ea -cp .build/classes:.build/test-classes dev.enola.be.task.TaskTest
java -ea -cp .build/classes:.build/test-classes dev.enola.be.task.TaskExecutorTest

if command -v pre-commit &> /dev/null
then
    # pre-commit run
    pre-commit install
fi
