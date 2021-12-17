package berlin.yuna.justlog.provider;

import berlin.yuna.justlog.logger.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static berlin.yuna.justlog.formatter.SimpleLogFormatter.NULL;

public class EmptyProvider extends Provider {

    public EmptyProvider() {
        this.name = NULL;
    }

    @Override
    public Provider compile(final Logger logger, final Map<Character, String> config) {
        return this;
    }

    @Override
    public String execute(
            final Supplier<String> message,
            final Supplier<Throwable> throwable,
            final Supplier<HashMap<String, String>> params
    ) {
        return "";
    }

    @Override
    public Provider refresh(final Supplier<Logger> logger) {
        return this;
    }

    @Override
    public char name() {
        return name;
    }
}
