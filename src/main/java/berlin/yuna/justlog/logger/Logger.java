package berlin.yuna.justlog.logger;

import berlin.yuna.justlog.config.LoggerConfig;
import berlin.yuna.justlog.config.LoggerConfigLoader;
import berlin.yuna.justlog.formatter.LogFormatter;
import berlin.yuna.justlog.formatter.SimpleLogFormatter;
import berlin.yuna.justlog.model.LogLevel;
import berlin.yuna.justlog.writer.LogWriter;
import berlin.yuna.justlog.writer.SimpleWriter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

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

    protected Logger() {
        this(getCaller(1).getClassName());
    }

    protected Logger(final Class<?> clazz) {
        this(clazz.getCanonicalName());
    }

    protected Logger(final String name) {
        this.name = name;
        this.config = new LoggerConfig(this);
        init();
    }

    public Logger init() {
        formatter(new SimpleLogFormatter());
        writer(new SimpleWriter());
        return updateConfig();
    }

    public String name() {
        return name;
    }

    public LogLevel level() {
        return level;
    }

    public Logger level(final LogLevel level) {
        this.level = level;
        return this;
    }

    public LogFormatter formatter() {
        return formatter;
    }

    public Logger formatter(final LogFormatter logFormatter) {
        this.formatter = logFormatter.logger(this).config(config);
        return this;
    }

    public LogWriter writer() {
        return writer;
    }

    public Logger writer(final LogWriter writer) {
        this.writer = writer.logger(this).config(config);
        return this;
    }

    public Logger config(final Map<String, String> config) {
        this.config.configLoader().putAll(config);
        return updateConfig();
    }

    public Logger updateConfig() {
        if (writer != null) {
            writer.config(config);
        }
        if (formatter != null) {
            formatter.config(config);
            formatter.pattern(config.getFormatterValue("pattern", formatter.getClass()).orElse("[%l{l=5}] [%d{p=HH:mm:ss}] [%c{l=10}] %m%n%e"));
        }
        this.level = config.getValue("level").filter(l -> !l.isBlank()).map(String::toUpperCase).map(LogLevel::valueOf).orElse(LogLevel.INFO);
        return this;
    }

    public LoggerConfigLoader configLoader() {
        return this.config.configLoader();
    }

    public void trace(final Supplier<String> msg) {
        trace(msg, null);
    }

    public void trace(final Supplier<String> msg, final Throwable t) {
        log(LogLevel.TRACE, msg, t);
    }

    public void debug(final Supplier<String> msg) {
        debug(msg, null);
    }

    public void debug(final Supplier<String> msg, final Throwable t) {
        log(LogLevel.DEBUG, msg, t);
    }

    public void info(final Supplier<String> msg) {
        info(msg, null);
    }

    public void info(final Supplier<String> msg, final Throwable t) {
        log(LogLevel.INFO, msg, t);
    }

    public void warn(final Supplier<String> msg) {
        info(msg, null);
    }

    public void warn(final Supplier<String> msg, final Throwable t) {
        log(LogLevel.WARN, msg, t);
    }

    public void error(final Supplier<String> msg) {
        error(msg, null);
    }

    public void error(final Supplier<String> msg, final Throwable t) {
        log(LogLevel.ERROR, msg, t);
    }

    public void fatal(final Supplier<String> msg) {
        fatal(msg, null);
    }

    public void fatal(final Supplier<String> msg, final Throwable t) {
        log(LogLevel.FATAL, msg, t);
    }

    //TODO: writer
    public void log(final LogLevel level, final Supplier<String> msg, final Throwable throwable) {
        if (this.level.ordinal() >= level.ordinal()) {
            if (level.ordinal() > 5) {
                writer.logOut(() -> formatter.format(level, msg.get(), throwable));
            } else {
                writer.logError(() -> formatter.format(level, msg.get(), throwable));
            }
        }
    }

    public boolean isRunning() {
        return executor.getActiveThreadCount() != 0;
    }

    public static Set<Logger> getAll() {
        return registry;
    }

    public static Logger get(final Class<?> clazz, final Supplier<Logger> fallback) {
        return get(clazz).orElseGet(fallback);
    }

    public static Optional<Logger> get(final Class<?> clazz) {
        final String className = getCaller(2).getClassName();
        return registry.stream().filter(logger -> logger.getClass() == clazz || logger.name().equals(className)).findFirst();
    }

    public static Logger get(final String name, final Supplier<Logger> fallback) {
        return get(name).orElseGet(() -> fallback == null ? null : addNoCheck(fallback.get()));
    }

    public static Logger add(final Logger logger) {
        return add(logger, false);
    }

    public static Logger add(final Logger logger, final boolean replace) {
        final Optional<Logger> previous = registry.stream().filter(item -> item.equals(logger)).findFirst();
        if (!replace && previous.isEmpty()) {
            return addNoCheck(logger);
        }
        return replace ? logger : previous.get();
    }

    public static Optional<Logger> get(final String name) {
        return registry.stream().filter(logger -> logger.name().equals(name)).findFirst();
    }

    public static Logger defaultLogger() {
        return DefaultLogger.instance(getCaller().getClassName());
    }

    public static StackTraceElement getCaller() {
        return getCaller(1);
    }

    public static StackTraceElement getCaller(final int traceId) {
        final int realTrace = traceId + 3;
        final StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        return trace.length <= realTrace ? trace[trace.length - 1] : trace[realTrace];
    }

    public static synchronized Executor getExecutor() {
        if (executor == null) {
            executor = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        }
        return executor;
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
