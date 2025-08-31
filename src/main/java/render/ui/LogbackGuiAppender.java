package render.ui;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class LogbackGuiAppender extends AppenderBase<ILoggingEvent> {
    private final LogWindow window;

    public LogbackGuiAppender(LogWindow window) {
        this.window = window;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        String msg = eventObject.getFormattedMessage();
        window.appendLine(eventObject.getLevel() + " " + eventObject.getLoggerName() + " - " + msg);
    }
}

