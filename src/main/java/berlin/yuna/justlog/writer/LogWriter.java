package berlin.yuna.justlog.writer;

import berlin.yuna.justlog.config.LoggerConfig;
import berlin.yuna.justlog.formatter.LogFormatter;
import berlin.yuna.justlog.logger.Logger;

import java.io.Serial;
import java.io.Serializable;
import java.util.function.Supplier;

public abstract class LogWriter implements Serializable {

    @Serial
    private static final long serialVersionUID = -352735044828046466L;
    protected Logger logger;

    /**
     * Possible configurations e.g. Encoding, TimeZone, BufferSize, etc.
     * @param config this will usually be set by the logger see {@link Logger#formatter(LogFormatter)} and {@link Logger#writer(LogWriter)} (LogFormatter)}
     * @return self
     */
    public abstract LogWriter config(final LoggerConfig config);

    public abstract LogWriter logOut(final Supplier<String> msg);

    public abstract LogWriter logError(final Supplier<String> msg);

    /**
     * @return {@link Logger}
     */
    public Logger logger() {
        return logger;
    }

    /**
     * Possible configurations e.g. Encoding, TimeZone, BufferSize, etc.
     * @param logger this will usually be set by the logger see {@link Logger#formatter(LogFormatter)} and {@link Logger#writer(LogWriter)} (LogFormatter)}
     * @return self
     */
    public LogWriter logger(final Logger logger) {
        this.logger = logger;
        return this;
    }
}
