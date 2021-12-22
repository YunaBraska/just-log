package berlin.yuna.justlog.formatter;

import berlin.yuna.justlog.config.LoggerConfig;
import berlin.yuna.justlog.logger.Logger;
import berlin.yuna.justlog.model.LogLevel;
import berlin.yuna.justlog.provider.AddressProvider;
import berlin.yuna.justlog.provider.ClassNameProvider;
import berlin.yuna.justlog.provider.DateFormatterProvider;
import berlin.yuna.justlog.provider.EmptyProvider;
import berlin.yuna.justlog.provider.ExceptionProvider;
import berlin.yuna.justlog.provider.HostnameProvider;
import berlin.yuna.justlog.provider.LineNumberProvider;
import berlin.yuna.justlog.provider.LogLevelProvider;
import berlin.yuna.justlog.provider.LoggerNameProvider;
import berlin.yuna.justlog.provider.MessageProvider;
import berlin.yuna.justlog.provider.MethodNameProvider;
import berlin.yuna.justlog.provider.NewLineProvider;
import berlin.yuna.justlog.provider.PidProvider;
import berlin.yuna.justlog.provider.Provider;
import berlin.yuna.justlog.writer.LogWriter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Character.isAlphabetic;

public abstract class LogFormatter implements Serializable {

    private static final long serialVersionUID = -7998184842762871108L;
    protected Logger logger;
    protected final List<Map.Entry<String, Provider>> patternCompiled = new ArrayList<>();
    protected static Set<Provider> providers;
    public static final char NULL = Character.MIN_VALUE;

    /**
     * Format log message
     *
     * @param level message log level
     * @param msg   message
     * @param t     exception
     * @return formatted String
     */
    public abstract String format(final LogLevel level, final String msg, final Throwable t);

    /**
     * Format log message
     *
     * @param level message log level
     * @param msg   message
     * @param t     exception
     * @return formatted Json String
     */
    public abstract String formatJson(final LogLevel level, final String msg, final Throwable t);

    /**
     * Possible configurations e.g. Encoding, TimeZone, BufferSize, etc.
     *
     * @param config this will usually be set by the logger see {@link Logger#formatter(LogFormatter)} and {@link Logger#writer(LogWriter)} (LogFormatter)}
     * @return self
     */
    public abstract LogFormatter config(final LoggerConfig config);

    /**
     * @return {@link Logger}
     */
    public Logger logger() {
        return logger;
    }

    /**
     * Possible configurations e.g. Encoding, TimeZone, BufferSize, etc.
     *
     * @param logger this will usually be set by the logger see {@link Logger#formatter(LogFormatter)} and {@link Logger#writer(LogWriter)} (LogFormatter)}
     * @return self
     */
    public LogFormatter logger(final Logger logger) {
        this.logger = logger;
        return this;
    }

    /**
     * Defines the pattern
     *
     * @param pattern input pattern
     * @return self
     */
    public LogFormatter pattern(final String pattern) {
        patternCompiled.clear();
        if (pattern != null && !pattern.isBlank()) {
            final char[] chars = pattern.toCharArray();
            int cursor = 0;
            final StringBuilder statics = new StringBuilder();
            while (cursor < chars.length) {
                final char cmd = getProviderId(chars, cursor);
                if (cmd != NULL) {
                    final Map<Character, String> params = new HashMap<>();
                    cursor = readParameter(chars, cursor + 2, params);
                    patternCompiled.add(Map.entry(statics.toString(), getProvider(cmd, params)));
                    statics.setLength(0);
                } else {
                    statics.append(charAt(chars, cursor));
                    cursor++;
                }
            }
            patternCompiled.add(Map.entry(statics.toString(), new EmptyProvider()));
        }
        return this;
    }

    /**
     * @return compiled pattern
     */
    public List<Map.Entry<String, Provider>> patternCompiled() {
        return new ArrayList<>(patternCompiled);
    }

    /**
     * Gets and configures a provider
     *
     * @param providerId provider id
     * @param config     provider config
     * @return self
     */
    protected Provider getProvider(final char providerId, final Map<Character, String> config) {
        return getProviders().stream()
                .filter(provider -> provider.id() == providerId)
                .map(provider -> provider.compile(this.logger(), config))
                .findFirst().orElseThrow(() -> new IllegalStateException("Unknown placeholder: " + providerId));
    }


    /**
     * Reads parameter from char array and add the result to the param map
     *
     * @param chars input array
     * @param index start to read at char index
     * @param param parameter map which will be filled during the process
     * @return new cursor value after moving to the parameter end
     */
    protected int readParameter(final char[] chars, final int index, final Map<Character, String> param) {
        if (!isNotEscaped(chars, index, '{')) {
            return index;
        }
        int cursor = index;
        char current;
        char key = NULL;
        final StringBuilder value = new StringBuilder();
        boolean stop;
        do {
            cursor++;
            current = charAt(chars, cursor);
            stop = isNotEscaped(chars, cursor, '}');
            final boolean isNextParam = isNotEscaped(chars, cursor, ',');
            if (stop) {
                cursor++;
            }
            if (key == NULL && isAlphabetic(current)) {
                key = current;
                cursor++;
            } else if (!stop && !isNextParam && key != NULL && current != NULL) {
                value.append(current);
            } else if (key != NULL && (stop || isNextParam)) {
                param.put(key, value.toString().trim());
                key = NULL;
                value.setLength(0);
            }
        } while (!stop && current != NULL);
        return cursor;
    }

    /**
     * gets the providerId
     *
     * @param input  charArray
     * @param cursor start to read at char index
     * @return returns providerId or else NULL char
     */
    protected char getProviderId(final char[] input, final int cursor) {
        return (charAt(input, cursor) == '%' && charAt(input, cursor - 1) != '\\') ? alphabeticCharAt(input, cursor + 1) : NULL;
    }

    /**
     * verifies if current char is not escaped
     *
     * @param input  charArray
     * @param cursor start to read at char index
     * @return true if the current char was escaped by a backslash
     */
    protected boolean isNotEscaped(final char[] input, final int cursor, final char c) {
        return cursor > -1 && cursor < input.length && charAt(input, cursor - 1) != '\\' && input[cursor] == c;
    }

    /**
     * gets char at cursor position
     *
     * @param input  charArray
     * @param cursor start to read at char index
     * @return returns char or else NULL char
     */
    protected char charAt(final char[] input, final int cursor) {
        return cursor > -1 && cursor < input.length ? input[cursor] : NULL;
    }

    /**
     * gets char if its alphabetic
     *
     * @param input  charArray
     * @param cursor start to read at char index
     * @return returns char if its alphabetic or else NULL char
     */
    protected char alphabeticCharAt(final char[] input, final int cursor) {
        return (cursor > -1 && cursor < input.length && isAlphabetic(input[cursor])) ? input[cursor] : NULL;
    }

    /**
     * add or overwrites a provider
     *
     * @param provider provider to add
     */
    public static synchronized void addProvider(final Provider provider) {
        providers.add(provider);
    }

    /**
     * Get all available providers
     *
     * @return set of providers
     */
    public static Set<Provider> getProviders() {
        if (providers == null) {
            providers = new HashSet<>(Set.of(
                    new AddressProvider(),
                    new ClassNameProvider(),
                    new DateFormatterProvider(),
                    new ExceptionProvider(),
                    new HostnameProvider(),
                    new LineNumberProvider(),
                    new LoggerNameProvider(),
                    new LogLevelProvider(),
                    new MessageProvider(),
                    new MethodNameProvider(),
                    new NewLineProvider(),
                    new PidProvider()
            ));
        }
        return new HashSet<>(providers);
    }

}
