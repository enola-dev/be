package ch.vorburger.exec;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;

// https://github.com/enola-dev/enola/blob/main/java/dev/enola/common/linereader/ExecutionContext.java
public interface StdIO {

    InputStream in();

    OutputStream out();

    OutputStream err();

    Charset inputCharset();

    Charset outputCharset();

    Charset errorCharset();

    default BufferedReader inReader() {
        return new BufferedReader(new InputStreamReader(in(), inputCharset()));
    }

    default PrintStream outPrintStream() {
        return new PrintStream(out(), true, outputCharset());
    }

    default PrintStream errPrintStream() {
        return new PrintStream(err(), true, errorCharset());
    }

    default String outString() {
        if (out() instanceof ByteArrayOutputStream baos) return baos.toString(outputCharset());
        throw new UnsupportedOperationException("out() is not a ByteArrayOutputStream");
    }

    default String errString() {
        if (err() instanceof ByteArrayOutputStream baos) return baos.toString(errorCharset());
        throw new UnsupportedOperationException("err() is not a ByteArrayOutputStream");
    }

    // https://github.com/enola-dev/enola/blob/main/java/dev/enola/common/linereader/SystemInOutIO.java
    static StdIO system() {
        // "stdin.encoding" is Java 21+ only; see https://github.com/openjdk/jdk/pull/25271
        var stdinEncoding = System.getProperty("stdin.encoding");
        if (stdinEncoding == null) stdinEncoding = System.getProperty("stdout.encoding");
        return of(
                System.in,
                Charset.forName(stdinEncoding),
                System.out,
                System.out.charset(),
                System.err,
                System.err.charset());
    }

    // https://github.com/enola-dev/enola/blob/main/java/dev/enola/common/linereader/TestIO.java
    static StdIO inMemory() {
        return inMemory(new ByteArrayInputStream(new byte[0]));
    }

    static StdIO inMemory(InputStream in) {
        return of(
                in, UTF_8, new ByteArrayOutputStream(), UTF_8, new ByteArrayOutputStream(), UTF_8);
    }

    static StdIO inMemory(byte[] bytes) {
        return inMemory(new ByteArrayInputStream(bytes.clone()));
    }

    static StdIO inMemory(List<String> lines) {
        var text = String.join(System.getProperty("line.separator"), lines);
        var bytes = text.getBytes(UTF_8);
        return inMemory(new ByteArrayInputStream(bytes));
    }

    static StdIO of(
            InputStream in,
            Charset inputCharset,
            OutputStream out,
            Charset outputCharset,
            OutputStream err,
            Charset errorCharset) {
        return new StdIO() {
            @Override
            public InputStream in() {
                return in;
            }

            @Override
            public OutputStream out() {
                return out;
            }

            @Override
            public OutputStream err() {
                return err;
            }

            @Override
            public Charset inputCharset() {
                return inputCharset;
            }

            @Override
            public Charset outputCharset() {
                return outputCharset;
            }

            @Override
            public Charset errorCharset() {
                return errorCharset;
            }
        };
    }
}
