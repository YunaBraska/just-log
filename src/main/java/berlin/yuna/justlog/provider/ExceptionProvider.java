package berlin.yuna.justlog.provider;

import berlin.yuna.justlog.logger.Logger;
import berlin.yuna.justlog.model.LogLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ExceptionProvider extends Provider {

    private Set<String> includes;

    public ExceptionProvider() {
        this.id = 'e';
        this.name = "exception";
    }

    @Override
    public Provider compile(final Logger logger, final Map<Character, String> config) {
        this.includes = new HashSet<>(getPattern(config).map(pattern -> List.of(pattern.split(";"))).orElse(new ArrayList<>()));
        return this;
    }

    @Override
    public String execute(
            final Supplier<LogLevel> level,
            final Supplier<String> message,
            final Supplier<Throwable> throwable,
            final Supplier<HashMap<String, String>> params
    ) {
        final Throwable th = throwable.get();
        if (th != null) {
            final String ths = stringOf(th);
            if (!includes.isEmpty()) {
                final StringBuilder result = new StringBuilder();
                for (final String line : ths.split(("\\r?\\n"))) {
                    final String trimLine = line.trim();
                    if (!trimLine.startsWith("at ") || includes.stream().anyMatch(pkg -> line.trim().startsWith("at " + pkg))) {
                        result.append(line).append(System.lineSeparator());
                    }
                }
                return result.toString();
            }
            return ths;
        }
        return "";
    }

    @Override
    public Provider refresh(final Supplier<Logger> logger) {
        return this;
    }
}
