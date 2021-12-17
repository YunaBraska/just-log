package berlin.yuna.justlog.provider;

import berlin.yuna.justlog.logger.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static berlin.yuna.justlog.formatter.SimpleLogFormatter.NULL;

public abstract class Provider {

    public static final char PH_PARAM_LENGTH = 'l';
    public static final char PH_PARAM_PATTERN = 'p';
    public static final char PH_PARAM_INDEX = 'i';
    public static final char PH_PARAM_ZONE_ID = 'z';

    protected char name = NULL;

    protected Provider() {
    }

    public abstract Provider compile(final Logger logger, final Map<Character, String> config);

    public abstract String execute(
            final Supplier<String> message,
            final Supplier<Throwable> throwable,
            final Supplier<HashMap<String, String>> params
    );

    public abstract Provider refresh(final Supplier<Logger> logger);

    public Optional<String> getValue(final Map<Character, String> config, final char name) {
        return config.entrySet().stream().filter(e -> e.getKey() == name).map(Map.Entry::getValue).findFirst();
    }

    public char name() {
        if (name == NULL) throw new NullPointerException("Name may not be null");
        return name;
    }

    protected Integer getLength(final Map<Character, String> config) {
        return getInteger(config, PH_PARAM_LENGTH);
    }

    protected Integer getIndex(final Map<Character, String> config) {
        return getInteger(config, PH_PARAM_INDEX);
    }

    private Integer getInteger(final Map<Character, String> config, final char name) {
        return getValue(config, name).map(Integer::valueOf).filter(i -> i > 1).orElse(-1);
    }

    protected Optional<String> getPattern(final Map<Character, String> config) {
        return getString(config, PH_PARAM_PATTERN);
    }

    protected Optional<String> getZoneId(final Map<Character, String> config) {
        return getString(config, PH_PARAM_ZONE_ID);
    }

    private Optional<String> getString(final Map<Character, String> config, final char key) {
        return getValue(config, key).stream().findFirst();
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

    protected String spaceUp(final String text, final int length) {
        if (length > 0 && text.length() < length) {
            final String spacePattern = "%-" + length + "s";
            return String.format(spacePattern, text);
        }
        return text;
    }

    protected String stringOf(final Throwable t) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    @Override
    public String toString() {
        return "Provider{" +
                "name=" + name +
                '}';
    }
}