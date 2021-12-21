package berlin.yuna.justlog.logger;

import berlin.yuna.justlog.config.LoggerConfig;
import berlin.yuna.justlog.config.LoggerConfigLoader;
import berlin.yuna.justlog.formatter.LogFormatter;
import berlin.yuna.justlog.formatter.SimpleLogFormatter;
import berlin.yuna.justlog.model.LogLevel;
import berlin.yuna.justlog.provider.Provider;
import berlin.yuna.justlog.writer.LogWriter;
import berlin.yuna.justlog.writer.SimpleWriter;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class Logger implements Serializable {

    @Serial
    private static final long serialVersionUID = 5540955522657213035L;
    protected LogWriter writer;
    protected LogFormatter formatter;
    protected LogLevel level;
    protected final String name;
    protected final LoggerConfig config;
    //TODO: configurable
    protected static ForkJoinPool executor;
    private static final Set<Logger> registry = ConcurrentHashMap.newKeySet();
    private static final Set<String> ignoreTraces = Set.of("org.junit.", "com.intellij.", Logger.class.getCanonicalName(), LogWriter.class.getPackageName(), LogFormatter.class.getPackageName(), Provider.class.getPackageName());

    protected Logger(final Class<?> clazz) {
        this(clazz.getCanonicalName());
    }

    protected Logger(final String name) {
        this.name = name;
        this.config = new LoggerConfig(this);
        init();
    }

    /**
     * Method invoked on initialisation
     *
     * @return self
     */
    public Logger init() {
        formatter(new SimpleLogFormatter());
        writer(new SimpleWriter());
        return updateConfig();
    }

    /**
     * @return Returns logger name
     */
    public String name() {
        return name;
    }

    /**
     * @return Returns log level
     */
    public LogLevel level() {
        return level;
    }

    /**
     * Sets the log level temporarily until {@link Logger#updateConfig()} - for permanent updates see {@link Logger#config(Map)}
     *
     * @param level new log level
     * @return self
     */
    public Logger level(final LogLevel level) {
        this.level = level;
        return this;
    }

    /**
     * @return Returns configured message formatter
     */
    public LogFormatter formatter() {
        return formatter;
    }

    /**
     * Sets the message formatter
     *
     * @param formatter new formatter
     * @return self
     */
    public Logger formatter(final LogFormatter formatter) {
        this.formatter = formatter.logger(this).config(config);
        return this;
    }

    /**
     * @return Returns configured message writer
     */
    public LogWriter writer() {
        return writer;
    }

    /**
     * Sets the configured message writer
     *
     * @param writer new writer
     * @return self
     */
    public Logger writer(final LogWriter writer) {
        this.writer = writer.logger(this).config(config);
        return this;
    }

    /**
     * adds or updates config values for the logger, formatter and writer
     *
     * @param config new configs
     * @return self
     */
    public Logger config(final Map<String, String> config) {
        this.config.configLoader().putAll(config);
        return updateConfig();
    }

    /**
     * adds or overwrites config values for the logger, formatter and writer
     *
     * @return self
     */
    public Logger updateConfig() {
        if (writer != null) {
            writer.config(config);
        }
        if (formatter != null) {
            formatter.config(config);
            formatter.pattern(config.getFormatterValue("pattern", formatter.getClass()).orElse("[%l{l=5}] [%d{p=HH:mm:ss}] [%c{l=10}] %m%n%e"));
        }
        level(config.getValue("level").filter(l -> !l.isBlank()).map(String::toUpperCase).map(LogLevel::valueOf).orElse(LogLevel.INFO));
        return this;
    }

    /**
     * @return Returns the config loader
     */
    public LoggerConfigLoader configLoader() {
        return this.config.configLoader();
    }

    /**
     * @param msg message which will only be loaded if {@link LogLevel#TRACE} is active
     */
    public void trace(final Supplier<String> msg) {
        trace(msg, null);
    }

    /**
     * @param msg message which will only be loaded if {@link LogLevel#TRACE} is active
     */
    public void trace(final Supplier<String> msg, final Supplier<Throwable> t) {
        log(LogLevel.TRACE, msg, t);
    }

    /**
     * @param msg message which will only be loaded if {@link LogLevel#DEBUG} is active
     */
    public void debug(final Supplier<String> msg) {
        debug(msg, null);
    }

    /**
     * @param msg message which will only be loaded if {@link LogLevel#DEBUG} is active
     */
    public void debug(final Supplier<String> msg, final Supplier<Throwable> t) {
        log(LogLevel.DEBUG, msg, t);
    }

    /**
     * @param msg message which will only be loaded if {@link LogLevel#INFO} is active
     */
    public void info(final Supplier<String> msg) {
        info(msg, null);
    }

    /**
     * @param msg message which will only be loaded if {@link LogLevel#INFO} is active
     */
    public void info(final Supplier<String> msg, final Supplier<Throwable> t) {
        log(LogLevel.INFO, msg, t);
    }

    /**
     * @param msg message which will only be loaded if {@link LogLevel#WARN} is active
     */
    public void warn(final Supplier<String> msg) {
        warn(msg, null);
    }

    /**
     * @param msg message which will only be loaded if {@link LogLevel#WARN} is active
     */
    public void warn(final Supplier<String> msg, final Supplier<Throwable> t) {
        log(LogLevel.WARN, msg, t);
    }

    /**
     * @param msg message which will only be loaded if {@link LogLevel#ERROR} is active
     */
    public void error(final Supplier<String> msg) {
        error(msg, null);
    }

    /**
     * @param msg message which will only be loaded if {@link LogLevel#ERROR} is active
     */
    public void error(final Supplier<String> msg, final Supplier<Throwable> t) {
        log(LogLevel.ERROR, msg, t);
    }

    /**
     * @param msg message which will only be loaded if {@link LogLevel#FATAL} is active
     */
    public void fatal(final Supplier<String> msg) {
        fatal(msg, null);
    }

    /**
     * @param msg message which will only be loaded if {@link LogLevel#FATAL} is active
     */
    public void fatal(final Supplier<String> msg, final Supplier<Throwable> t) {
        log(LogLevel.FATAL, msg, t);
    }

    /**
     * logs message using {@link Logger#formatter} and {@link Logger#writer}
     *
     * @param level     log level to verify if message should be logged
     * @param msg       msg message which will only be loaded if level is active
     * @param throwable optional if an exception happened
     */
    public void log(final LogLevel level, final Supplier<String> msg, final Supplier<Throwable> throwable) {
        if (this.level.ordinal() >= level.ordinal()) {
            if (level.ordinal() < 5) {
                writer.logOut(() -> formatter.format(level, msg == null ? null : msg.get(), throwable == null ? null : throwable.get()));
            } else {
                writer.logError(() -> formatter.format(level, msg == null ? null : msg.get(), throwable == null ? null : throwable.get()));
            }
        }
    }

    /**
     * @return all every registered logger
     */
    public static Set<Logger> getAll() {
        return new HashSet<>(registry);
    }

    /**
     * Removes logger from registry
     *
     * @param logger Logger to be removed from this set, if present
     * @return Returns: true if this set contained the specified element
     */
    public static synchronized boolean remove(final Logger logger) {
        return registry.remove(logger);
    }

    /**
     * Registers a new logger
     *
     * @param logger new logger to register
     * @return new logger or previous logger if there is already a registered one
     */
    public static Logger add(final Logger logger) {
        return add(logger, false);
    }

    /**
     * Registers a new logger
     *
     * @param logger  new logger to register
     * @param replace replace old logger if the same logger already exists
     * @return new logger or previous logger if there is already a registered one and replace is set to false
     */
    public static Logger add(final Logger logger, final boolean replace) {
        final Optional<Logger> previous = registry.stream().filter(item -> item.equals(logger)).findFirst();
        if (!replace && previous.isEmpty()) {
            return addNoCheck(logger);
        }
        return replace ? logger : previous.get();
    }

    /**
     * @return default logger
     */
    public static Logger defaultLogger() {
        return DefaultLogger.instance(getCaller().getClassName());
    }

    /**
     * @return default logger
     */
    public static Logger defaultLogger(final String name) {
        return DefaultLogger.instance(name);
    }

    /**
     * @return caller id aka {@link StackTraceElement}
     */
    public static StackTraceElement getCaller() {
        return getCaller(1);
    }

    /**
     * @param traceId index of caller
     * @return caller id aka {@link StackTraceElement}
     */
    public static StackTraceElement getCaller(final int traceId) {
        final StackTraceElement[] traces = Thread.getAllStackTraces().values().stream().flatMap(Stream::of)
                .filter(t -> t.getModuleName() == null)
                .filter(t -> ignoreTraces.stream().noneMatch(ignoreTrace -> t.getClassName().startsWith(ignoreTrace)))
                .toArray(StackTraceElement[]::new);
        return traceId >= traces.length ? traces[traces.length - 1] : traces[traceId];
    }

    /**
     * Initialises new Executor if not present
     *
     * @return Executor
     */
    public static synchronized Executor getExecutor() {
        if (executor == null) {
            executor = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        }
        return executor;
    }

    /**
     * @return true if the {@link Logger#executor} is used and active threads are running
     */
    public static boolean isRunning() {
        return executor != null && executor.getActiveThreadCount() != 0;
    }


    private static Logger addNoCheck(final Logger logger) {
        registry.add(logger);
        return logger;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Logger logger = (Logger) o;
        return Objects.equals(name, logger.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Logger{" +
                "logger='" + this.getClass().getSimpleName() + '\'' +
                ", name=" + name +
                '}';
    }
}
