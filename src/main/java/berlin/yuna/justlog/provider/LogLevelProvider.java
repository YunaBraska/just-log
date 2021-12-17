package berlin.yuna.justlog.provider;

import berlin.yuna.justlog.logger.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class LogLevelProvider extends Provider {

    private int length;
    private Logger logger;

    public LogLevelProvider() {
        this.name = 'l';
    }

    @Override
    public Provider compile(final Logger logger, final Map<Character, String> config) {
        this.length = getLength(config);
        this.logger = logger;
        return this;
    }

    @Override
    public String execute(
            final Supplier<String> message,
            final Supplier<Throwable> throwable,
            final Supplier<HashMap<String, String>> params
    ) {
        return spaceUp(logger.level().name(), length);
    }

    @Override
    public Provider refresh(final Supplier<Logger> logger) {
        return this;
    }
}
