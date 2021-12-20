package berlin.yuna.justlog.provider;

import berlin.yuna.justlog.logger.Logger;
import berlin.yuna.justlog.model.LogLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MessageProvider extends Provider {

    public MessageProvider() {
        this.id = 'm';
        this.name = "message";
    }

    @Override
    public Provider compile(final Logger logger, final Map<Character, String> config) {
        return this;
    }

    @Override
    public String execute(
            final Supplier<LogLevel> level,
            final Supplier<String> message,
            final Supplier<Throwable> throwable,
            final Supplier<HashMap<String, String>> params
    ) {
        return message.get();
    }

    @Override
    public Provider refresh(final Supplier<Logger> logger) {
        return this;
    }
}
