package berlin.yuna.justlog.provider;

import berlin.yuna.justlog.logger.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PidProvider extends Provider {

    private int length;

    public PidProvider() {
        this.name = 'p';
    }

    @Override
    public Provider compile(final Logger logger, final Map<Character, String> config) {
        this.length = getLength(config);
        return this;
    }

    @Override
    public String execute(
            final Supplier<String> message,
            final Supplier<Throwable> throwable,
            final Supplier<HashMap<String, String>> params
    ) {
        return spaceUp(String.valueOf(ProcessHandle.current().pid()), length);
    }

    @Override
    public Provider refresh(final Supplier<Logger> logger) {
        return this;
    }
}
