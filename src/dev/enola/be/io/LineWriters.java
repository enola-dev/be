package dev.enola.be.io;

import java.io.IOException;

public final class LineWriters {

    public static final LineWriter NOOP = _ -> {};
    public static final LineWriter SYSTEM_OUT = from(System.out);
    public static final LineWriter SYSTEM_ERR = from(System.err);

    public static LineWriter from(Appendable appendable) {
        return new LineWriter() {
            @Override
            public void println(Object line) throws IOException {
                appendable.append(line.toString()).append(System.lineSeparator());
            }
        };
    }

    private LineWriters() {}
}
