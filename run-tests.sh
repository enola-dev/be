#!/usr/bin/env bash
set -euo pipefail

rm -rf /tmp/be-classes /tmp/be-test-classes
mkdir -p /tmp/be-classes /tmp/be-test-classes

find src -name '*.java' ! -name '*Test.java' ! -name 'Test*.java' -print0 | xargs -0 javac -d /tmp/be-classes

find test -name '*Test.java' -o -name 'Test*.java' | xargs javac -cp /tmp/be-classes -d /tmp/be-test-classes

java -ea -cp /tmp/be-classes:/tmp/be-test-classes dev.enola.be.task.StatusTest
java -ea -cp /tmp/be-classes:/tmp/be-test-classes dev.enola.be.task.TaskTest
java -ea -cp /tmp/be-classes:/tmp/be-test-classes dev.enola.be.task.TaskExecutorTest
