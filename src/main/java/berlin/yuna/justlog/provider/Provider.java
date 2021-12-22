package berlin.yuna.justlog.provider;

import berlin.yuna.justlog.logger.Logger;
import berlin.yuna.justlog.model.LogLevel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static berlin.yuna.justlog.formatter.SimpleLogFormatter.NULL;

public abstract class Provider {

    public static final char PH_PARAM_LENGTH = 'l';
    public static final char PH_PARAM_PATTERN = 'p';
    public static final char PH_PARAM_INDEX = 'i';
    public static final char PH_PARAM_ZONE_ID = 'z';

    protected char id = NULL;
    protected String name = null;

    protected Provider() {
    }

    public abstract Provider compile(final Logger logger, final Map<Character, String> config);

    public abstract String execute(
            final Supplier<LogLevel> level,
            final Supplier<String> message,
            final Supplier<Throwable> throwable,
            final Supplier<HashMap<String, String>> params
    );

    public abstract Provider refresh(final Supplier<Logger> logger);

    public Optional<String> getValue(final Map<Character, String> config, final char name) {
        return config.entrySet().stream().filter(e -> e.getKey() == name).map(Map.Entry::getValue).findFirst();
    }

    public char id() {
        if (id == NULL) throw new NullPointerException("Provider [id] may not be [null]");
        return id;
    }

    public String name() {
        if (name == null) throw new NullPointerException("Provider [name] may not be [null]");
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
        return result.length() == 0 ? text : result.append(text.substring(indexPrev + 1)).toString();
    }

    /**
     * Trims or adds length to a string
     *
     * @param text   input string
     * @param length resize length
     * @return manipulated string
     */
    protected String spaceUp(final String text, final int length) {
        if (length > 0 && text.length() < length) {
            final String spacePattern = "%-" + length + "s";
            return String.format(spacePattern, text);
        }
        return text;
    }

    /**
     * Prints a Throwable to string
     *
     * @param t input
     * @return throwable as string;
     */
    public static String stringOf(final Throwable t) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Provider provider = (Provider) o;
        return id == provider.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Provider{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
