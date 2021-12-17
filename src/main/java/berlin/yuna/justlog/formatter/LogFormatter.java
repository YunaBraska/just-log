package berlin.yuna.justlog.formatter;

import berlin.yuna.justlog.LogLevel;
import berlin.yuna.justlog.logger.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class LogFormatter {

    protected Logger logger;
    protected String pattern = "[%l] [%d] [%c] %m%n%e";
    public static final Pattern PATTERN_OPTIONS = Pattern.compile("(?<format>(?<cmd>\\%.)\\{(.*?)\\})");
    public static final Pattern PATTERN_OPTION = Pattern.compile("(?<key>.)=(?<value>.*?)(,|})");

    @Deprecated
    public abstract LogFormatter compilePattern(final String[] pattern);

    @Deprecated
    public abstract LogFormatter afterPattern();

    public abstract String format(final LogLevel level, final String text, final Throwable t);

    public abstract LogFormatter config(final Map<String, String> config);

    public Logger logger() {
        return logger;
    }

    public LogFormatter logger(final Logger pLogger) {
        this.logger = pLogger;
        return this;
    }


    public LogFormatter pattern(final String pattern) {
        final String tmpPattern = pattern == null || pattern.isBlank() ? null : pattern;
        if (tmpPattern == null) {
            this.pattern = pattern;
            return this;
        }
        final Matcher optionsMatcher = PATTERN_OPTIONS.matcher(pattern);
        while (optionsMatcher.find()) {
            final String options = optionsMatcher.group("format");
            final char cmd = optionsMatcher.group("cmd").charAt(1);
            final Matcher option = PATTERN_OPTION.matcher(options);
            while (option.find()) {
                compilePattern(new String[]{
                        String.valueOf(cmd),
                        String.valueOf(option.group("key").charAt(0)),
                        option.group("value")
                });
            }
        }
        this.pattern = pattern;
        return afterPattern();
    }

    protected String spaceUp(final String text, final int length) {
        if (text.length() < length) {
            final String spacePattern = "%-" + length + "s";
            return String.format(spacePattern, text);
        }
        return text;
    }

    protected String shortenPackage(final String text, final int maxLength) {
        final StringBuilder result = new StringBuilder();
        int index = text.indexOf('.');
        int indexPrev = -1;
        while (index != -1 && (text.length() - (index - result.length())) > maxLength) {
            result.append(text.charAt(indexPrev + 1)).append('.');
            indexPrev = index;
            index = text.indexOf('.', index + 1);
        }
        return result.isEmpty() ? text : result.append(text.substring(indexPrev + 1)).toString();
    }

    protected String stringOf(final Throwable t) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

}
