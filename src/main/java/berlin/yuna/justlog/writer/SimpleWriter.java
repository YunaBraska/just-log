package berlin.yuna.justlog.writer;

import berlin.yuna.justlog.config.LoggerConfig;
import berlin.yuna.justlog.formatter.LogFormatter;
import berlin.yuna.justlog.logger.Logger;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

//FIXME: improvements: https://github.com/LMAX-Exchange/disruptor
public class SimpleWriter extends LogWriter implements Serializable {

    @Serial
    private static final long serialVersionUID = -5437769891469935281L;
    private final Executor executor;

    public SimpleWriter() {
        executor = Logger.getExecutor();
    }

    /**
     * Possible configurations e.g. Encoding, TimeZone, BufferSize, etc.
     *
     * @param config this will usually be set by the logger see {@link Logger#formatter(LogFormatter)} and {@link Logger#writer(LogWriter)} (LogFormatter)}
     * @return self
     */
    @Override
    public LogWriter config(final LoggerConfig config) {
        return this;
    }

    @Override
    public LogWriter logOut(final Supplier<String> msg) {
        executor.execute(() -> System.out.print(msg.get()));
        return this;
    }

    @Override
    public LogWriter logError(final Supplier<String> msg) {
        executor.execute(() -> System.err.print(msg.get()));
        return this;
    }
}
