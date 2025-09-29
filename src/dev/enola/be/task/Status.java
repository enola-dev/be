package dev.enola.be.task;

/**
 * <a href=
 * "https://www.mermaidchart.com/d/ef337271-7f85-4a77-8353-9e4a880efe2a">Status</a>.
 */
public enum Status {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED,
    TIMED_OUT;

    public boolean isTerminal() {
        return switch (this) {
            case COMPLETED, FAILED, CANCELLED, TIMEDOUT -> true;
            case PENDING, IN_PROGRESS -> false;
        };
    }
}
