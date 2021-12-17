package berlin.yuna.justlog.formatter;

import berlin.yuna.justlog.LogLevel;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static berlin.yuna.justlog.logger.Logger.getCaller;

@Deprecated
public class SimpleLogFormatterOld extends LogFormatter {

    private String loggerName;
    private ZoneId timeZone = ZoneId.of("UTC");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
    private final Map<Character, Integer> lengthMap = new HashMap<>();
    private final Map<Character, Integer> integerMap = new HashMap<>();
    private final Map<Character, String> stringMap = new HashMap<>();

    //pattern = "[%p{l=10}] [%d{p=HH:mm:ss.SSS}] [%c{l=10}] [%T{l=10,i=3}:%M:%L] %m"

    //l = log level
    //m = message
    //n = new line
    //p = pid
    //c = logger name
    //d = date (DateTimeFormatter)
    //e = exception
    //T = trace / caller
    //L = trace / caller line number

    //i = index
    //l = length
    //p = pattern

    //TODO: compile to KV map (K = pre String, v = function variable)
    //  Loop over KV and build string instead of char loop
    public String format(final LogLevel level, final String text, final Throwable t) {
        if (pattern == null) {
            return t == null ? text : text + System.lineSeparator() + stringOf(t);
        }
        final StringBuilder result = new StringBuilder();
        final int length = pattern.length();
        int i = 0;
        while (i < length) {
            final boolean hasPlaceHolder = pattern.charAt(i) == '%';
            final char key = hasPlaceHolder && i + 1 <= length ? pattern.charAt(i + 1) : pattern.charAt(i);
            if (hasPlaceHolder) {
                i++;
                final StackTraceElement trace = (key == 'T' || key == 'M' || key == 'L') ? getCaller(integerMap.getOrDefault(key, 0)) : null;
                switch (key) {
                    case 'c' -> result.append(loggerName);
                    case 'd' -> result.append(timeFormatter.format(LocalDateTime.now(timeZone)));
                    case 'e' -> result.append(t == null ? "" : applyThrowable(key, stringOf(t)));
                    case 'l' -> result.append(applyFormat(key, level.name()));
                    case 'm' -> result.append(text);
                    case 'n' -> result.append(System.lineSeparator());
                    case 'L' -> result.append(applyFormat(key, String.valueOf(trace.getLineNumber())));
                    case 'T' -> result.append(applyFormat(key, trace.getClassName()));
                    case 'M' -> result.append(applyFormat(key, trace.getMethodName()));
                    case 'p' -> result.append(ProcessHandle.current().pid());
                    default -> result.append(key);
                }
            } else {
                result.append(key);
            }
            i++;
        }

        return result.toString();
    }

    @Override
    public LogFormatter config(final Map<String, String> config) {
        return this;
    }

    public LogFormatter compilePattern(final String[] pattern) {
        final char ns = pattern[0].charAt(0);
        final char paramKey = pattern[1].charAt(0);
        final String paramValue = pattern[2];
        if (ns == 'd' && paramKey == 'p') {
            timeFormatter = DateTimeFormatter.ofPattern(paramValue);
        } else {
            switch (paramKey) {
                case 'p' -> stringMap.put(ns, paramValue);
                case 'l' -> lengthMap.put(ns, Integer.valueOf(paramValue));
                case 'i' -> integerMap.put(ns, Integer.valueOf(paramValue));
                default -> throw new IllegalStateException("Unexpected value: " + paramKey);
            }
        }
        return this;
    }

    public LogFormatter afterPattern() {
        this.loggerName = applyFormat('c', logger.name());
        this.pattern = pattern.replaceAll(PATTERN_OPTIONS.pattern(), "$2");
        return this;
    }

    protected String applyFormat(final char key, final String text) {
        final int length = lengthMap.getOrDefault(key, -1);
        if (length > 1) {
            if (text.length() < length) {
                return spaceUp(text, length);
            } else {
                return spaceUp(shortenPackage(text, length), length);
            }
        }
        return text;
    }

    protected String applyThrowable(final char key, final String text) {
        final String packageList = stringMap.get(key);
        if (packageList != null) {
            final StringBuilder result = new StringBuilder();
            for (final String line : text.split(("\\r?\\n"))) {
                final String trimLine = line.trim();
                if (!trimLine.startsWith("at ") || Arrays.stream(packageList.split("(,|;)")).anyMatch(pkg -> line.trim().startsWith("at " + pkg))) {
                    result.append(line).append(System.lineSeparator());
                }
            }
            return result.toString();
        }
        return text;
    }

}
