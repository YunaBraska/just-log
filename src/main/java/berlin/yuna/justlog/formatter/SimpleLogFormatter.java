package berlin.yuna.justlog.formatter;

import berlin.yuna.justlog.config.LoggerConfig;
import berlin.yuna.justlog.logger.Logger;
import berlin.yuna.justlog.model.LogLevel;
import berlin.yuna.justlog.provider.Provider;
import berlin.yuna.justlog.writer.LogWriter;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static berlin.yuna.justlog.provider.Provider.PH_PARAM_INDEX;
import static berlin.yuna.justlog.provider.Provider.stringOf;

@SuppressWarnings("java:S1948")
public class SimpleLogFormatter extends LogFormatter {

    @Serial
    private static final long serialVersionUID = -7393578204904822373L;
    private int lastTraceId = 0;

    /**
     * Format log message
     *
     * @param level message log level
     * @param msg   message
     * @param t     exception
     * @return formatted String
     */
    public String format(final LogLevel level, final String msg, final Throwable t) {
        final StringBuilder result = new StringBuilder();
        if (patternCompiled.isEmpty()) {
            result.append(t == null ? msg + System.lineSeparator() : msg + System.lineSeparator() + stringOf(t));
        } else {
            patternCompiled.forEach(e ->
                    result.append(e.getKey()).append(e.getValue().execute(
                            () -> level,
                            () -> msg,
                            () -> t,
                            HashMap::new
                    )));
        }
        return result.toString();
    }

    /**
     * Format log message
     *
     * @param level message log level
     * @param msg   message
     * @param t     exception
     * @return formatted Json String
     */
    @Override
    public String formatJson(final LogLevel level, final String msg, final Throwable t) {
        final JsonObjectBuilder builder = Json.createObjectBuilder();
        if (patternCompiled.isEmpty()) {
            builder.add("message", msg == null ? "null" : msg);
            Optional.ofNullable(t).ifPresent(ex -> builder.add("exception", stringOf(ex)));
        } else {
            patternCompiled.forEach(e -> {
                if (e.getValue().name() != null) {
                    builder.add(e.getValue().name(), e.getValue().execute(
                            () -> level,
                            () -> msg,
                            () -> t,
                            HashMap::new
                    ));
                }
            });
        }
        return builder.build().toString();
    }

    /**
     * Possible configurations e.g. Encoding, TimeZone, BufferSize, etc.
     *
     * @param config this will usually be set by the logger see {@link Logger#formatter(LogFormatter)} and {@link Logger#writer(LogWriter)} (LogFormatter)}
     * @return self
     */
    @Override
    public LogFormatter config(final LoggerConfig config) {
        return this;
    }

    /**
     * Gets and configures a provider
     *
     * @param providerId provider id
     * @param config     provider config
     * @return self
     */
    @Override
    protected Provider getProvider(final char providerId, final Map<Character, String> config) {
        applyTraceIdIfNotSet(providerId, config);
        return super.getProvider(providerId, config);
    }

    private void applyTraceIdIfNotSet(final char namePH, final Map<Character, String> config) {
        if (namePH == 'T' || namePH == 'M' || namePH == 'L') {
            final Integer traceId = config.entrySet().stream().filter(e -> e.getKey() == PH_PARAM_INDEX).findFirst().map(e -> Integer.valueOf(e.getValue())).orElse(-1);
            if (traceId == -1) {
                config.put(PH_PARAM_INDEX, String.valueOf(lastTraceId));
            } else {
                lastTraceId = traceId;
            }
        }
    }

}
