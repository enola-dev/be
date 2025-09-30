#!/usr/bin/env bash
# Wrapper script for palantir-java-format to be used with pre-commit
set -e

VERSION="${PALANTIR_JAVA_FORMAT_VERSION:-2.50.0}"
CACHE_DIR="${HOME}/.cache/palantir-java-format"
JAR="${CACHE_DIR}/palantir-java-format-${VERSION}.jar"
DEPS_DIR="${CACHE_DIR}/deps-${VERSION}"

# Create cache directory
mkdir -p "${CACHE_DIR}"
mkdir -p "${DEPS_DIR}"

# Download main jar if not exists
if [ ! -f "${JAR}" ]; then
    echo "Downloading palantir-java-format ${VERSION}..." >&2
    curl -sSL "https://repo1.maven.org/maven2/com/palantir/javaformat/palantir-java-format/${VERSION}/palantir-java-format-${VERSION}.jar" -o "${JAR}"
fi

# Download required dependencies if not exist
download_dep() {
    local group=$1
    local artifact=$2
    local version=$3
    local group_path=${group//./\/}
    local jar_file="${DEPS_DIR}/${artifact}-${version}.jar"
    
    if [ ! -f "${jar_file}" ]; then
        echo "Downloading ${artifact} ${version}..." >&2
        curl -sSL "https://repo1.maven.org/maven2/${group_path}/${artifact}/${version}/${artifact}-${version}.jar" -o "${jar_file}"
    fi
}

# Download SPI dependency
download_dep "com.palantir.javaformat" "palantir-java-format-spi" "${VERSION}"

# Download Guava (required dependency)
download_dep "com.google.guava" "guava" "33.0.0-jre"

# Download other transitive dependencies
download_dep "com.google.guava" "failureaccess" "1.0.2"
download_dep "com.google.errorprone" "error_prone_annotations" "2.11.0"
download_dep "com.fasterxml.jackson.core" "jackson-databind" "2.19.2"
download_dep "com.fasterxml.jackson.core" "jackson-core" "2.19.2"
download_dep "com.fasterxml.jackson.core" "jackson-annotations" "2.19.2"
download_dep "com.fasterxml.jackson.datatype" "jackson-datatype-guava" "2.19.2"
download_dep "com.fasterxml.jackson.datatype" "jackson-datatype-jdk8" "2.19.2"
download_dep "com.fasterxml.jackson.datatype" "jackson-datatype-jsr310" "2.19.2"
download_dep "org.derive4j" "derive4j" "1.1.1"
download_dep "org.functionaljava" "functionaljava" "4.8"
download_dep "org.slf4j" "slf4j-api" "2.0.17"
download_dep "org.slf4j" "slf4j-simple" "2.0.17"

# Build classpath
CLASSPATH="${JAR}"
for jar in "${DEPS_DIR}"/*.jar; do
    CLASSPATH="${CLASSPATH}:${jar}"
done

# Run formatter with necessary JDK module exports
java --add-exports jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED \
     --add-exports jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED \
     --add-exports jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED \
     --add-exports jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED \
     --add-exports jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED \
     --add-exports jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED \
     -cp "${CLASSPATH}" com.palantir.javaformat.java.Main --replace "$@"
