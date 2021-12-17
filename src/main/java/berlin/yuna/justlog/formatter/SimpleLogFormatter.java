package berlin.yuna.justlog.formatter;

import berlin.yuna.justlog.LogLevel;
import berlin.yuna.justlog.logger.Logger;
import berlin.yuna.justlog.provider.EmptyProvider;
import berlin.yuna.justlog.provider.Provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

import static berlin.yuna.justlog.provider.Provider.PH_PARAM_INDEX;
import static java.lang.Character.isAlphabetic;

public class SimpleLogFormatter extends LogFormatter {

    private int lastTraceId = 0;
    private final List<Map.Entry<String, Provider>> patternCompiled = new ArrayList<>();
    public static final char NULL = Character.MIN_VALUE;
    private final Executor executor;

    //pattern = "[%p{l=10}] [%d{p=HH:mm:ss.SSS}] [%c{l=10}] [%T{l=10,i=3}:%M:%L] %m"

    //l = log level
    //m = message
    //n = new line
    //p = pid
    //c = logger name
    //d = date (DateTimeFormatter)
    //e = exception
    //a = address
    //h = hostname
    //T = trace / caller
    //M = trace / caller method
    //L = trace / caller line number

    //i = index
    //l = length
    //p = pattern


    public SimpleLogFormatter() {
        this.executor = Logger.getExecutor();
    }

    private final HashMap<String, String> config = new HashMap<>();

    public String format(final LogLevel level, final String text, final Throwable t) {
        if (pattern == null) {
            return t == null ? text : text + System.lineSeparator() + stringOf(t);
        }
        final StringBuilder result = new StringBuilder();
        executor.execute(() -> patternCompiled.forEach(e -> result.append(e.getKey()).append(e.getValue().execute(() -> text, () -> t, () -> config))));
        return result.toString();
    }

    @Override
    public LogFormatter config(final Map<String, String> config) {
        return this;
    }

    private char cmdChar(final char[] input, final int index) {
        return (charAt(input, index) == '%' && charAt(input, index - 1) != '\\') ? alphabeticCharAt(input, index + 1) : NULL;
    }

    private boolean isNotEscaped(final char[] input, final int cursor, final char c) {
        return cursor > -1 && cursor < input.length && charAt(input, cursor - 1) != '\\' && input[cursor] == c;
    }

    private char charAt(final char[] input, final int index) {
        return index > -1 && index < input.length ? input[index] : NULL;
    }

    private char alphabeticCharAt(final char[] input, final int index) {
        return (index > -1 && index < input.length && isAlphabetic(input[index])) ? input[index] : NULL;
    }

    @Override
    public LogFormatter pattern(final String pattern) {
        if (pattern != null && !pattern.isBlank()) {
            final char[] chars = pattern.toCharArray();
            int cursor = 0;
            patternCompiled.clear();
            final StringBuilder statics = new StringBuilder();
            while (cursor < chars.length) {
                final char cmd = cmdChar(chars, cursor);
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

    protected Optional<String> getParam(final List<String[]> params, final String param) {
        return params.stream().filter(map -> map[0].equals(param)).map(map -> map[1]).findFirst();
    }

    protected String applySize(final String text, final List<String[]> params) {
        final int length = getParam(params, "l").map(Integer::valueOf).filter(i -> i > 1).orElse(-1);
        if (length > 1) {
            if (text.length() < length) {
                return spaceUp(text, length);
            } else {
                return spaceUp(shortenPackage(text, length), length);
            }
        }
        return text;
    }

    protected Provider getProvider(final char namePH, final Map<Character, String> config) {
        applyTraceIdIfNotSet(namePH, config);
        return Logger.getDefaultProvider().stream()
                .filter(provider -> provider.name() == namePH)
                .map(provider -> provider.compile(this.logger(), config))
                .findFirst().orElseThrow(() -> new IllegalStateException("Unknown placeholder: " + namePH));
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

    @Override
    public LogFormatter compilePattern(final String[] pattern) {
        return this;
    }

    public LogFormatter afterPattern() {
        return this;
    }

}
