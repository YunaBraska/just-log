package berlin.yuna.justlog.config;

import berlin.yuna.justlog.logger.Logger;

import java.util.Objects;
import java.util.Optional;

public class LoggerConfig {

    private final Logger logger;
    private final LoggerConfigLoader config;

    public LoggerConfig(final Logger logger) {
        this.logger = logger;
        this.config = LoggerConfigLoader.instance();
        config.get("file", logger.name(), logger.getClass().getSimpleName(), null).ifPresent(config::putPaths);
    }

    public Optional<String> getValue(final String key) {
        return config.get(key, logger.name(), logger.getClass().getSimpleName().toLowerCase(), null);
    }

    public Optional<String> getFormatterValue(final String key, final Class<?> subClass) {
        return getValue(key, subClass).or(() -> getValue(key, "formatter"));
    }

    public Optional<String> getWriterValue(final String key, final Class<?> subClass) {
        return getValue(key, subClass).or(() -> getValue(key, "writer"));
    }

    public Optional<String> getValue(final String key, final Class<?> subClass) {
        return getValue(key, subClass.getSimpleName().toLowerCase());
    }

    public Optional<String> getValue(final String key, final String subClassName) {
        return config.get(key, logger.name(), logger.getClass().getSimpleName().toLowerCase(), subClassName);
    }

    public LoggerConfigLoader configLoader() {
        return config;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final LoggerConfig that = (LoggerConfig) o;
        return Objects.equals(logger, that.logger);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), logger);
    }

    @Override
    public String toString() {
        return "LoggerConfig{" +
                "logger=" + logger +
                '}';
    }
}
