package berlin.yuna.justlog.provider;

import berlin.yuna.justlog.logger.Logger;
import berlin.yuna.justlog.model.LogLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static berlin.yuna.justlog.logger.Logger.getCaller;

public class MethodNameProvider extends Provider {

    private int length;
    private int traceIndex;

    public MethodNameProvider() {
        this.id = 'M';
        this.name = "Method";
    }

    @Override
    public Provider compile(final Logger logger, final Map<Character, String> config) {
        this.length = getLength(config);
        this.traceIndex = getIndex(config) == -1 ? 0 : getIndex(config);
        return this;
    }

    @Override
    public String execute(
            final Supplier<LogLevel> level,
            final Supplier<String> message,
            final Supplier<Throwable> throwable,
            final Supplier<HashMap<String, String>> params
    ) {
        return spaceUp(getCaller(traceIndex).getMethodName(), length);
    }

    @Override
    public Provider refresh(final Supplier<Logger> logger) {
        return this;
    }
}
