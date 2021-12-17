package berlin.yuna.justlog.provider;

import berlin.yuna.justlog.logger.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class LoggerNameProvider extends Provider {

    private int length;
    private String loggerName;

    public LoggerNameProvider() {
        this.name = 'c';
    }

    @Override
    public Provider compile(final Logger logger, final Map<Character, String> config) {
        this.length = getLength(config);
        loggerName = getLoggerName(logger, length);
        return this;
    }

    @Override
    public String execute(
            final Supplier<String> message,
            final Supplier<Throwable> throwable,
            final Supplier<HashMap<String, String>> params
    ) {
        return loggerName;
    }

    @Override
    public Provider refresh(final Supplier<Logger> logger) {
        this.loggerName = getLoggerName(logger.get(), length);
        return this;
    }

    private String getLoggerName(final Logger logger, final Integer length) {
        return spaceUp(shortenPackage(logger.name(), length), length);
    }

}
