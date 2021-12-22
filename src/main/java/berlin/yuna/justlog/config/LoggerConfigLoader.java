package berlin.yuna.justlog.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static java.util.logging.Logger.getLogger;

public class LoggerConfigLoader extends HashMap<String, String> implements Serializable {

    public static final String LOGGER_PREFIX = "logger.";
    private static final long serialVersionUID = -6613899829390043425L;
    private static LoggerConfigLoader loader = null;

    /**
     * Singleton - This will read the logger config only for the first call
     * Loaded Logfile order ["application.properties" and "logger.properties"]
     * Loaded LogFileLocation order [classpath:resource, userDir, userDir:config]
     * To add more config pats see {@link LoggerConfigLoader#put(Properties)}, {@link LoggerConfigLoader#put(InputStream)}, {@link LoggerConfigLoader#put(Path)},  {@link LoggerConfigLoader#putPaths(String...)}
     *
     * @return always same instance
     */
    public static LoggerConfigLoader instance() {
        if (loader == null) {
            loader = new LoggerConfigLoader();
        }
        return loader;
    }

    /**
     * Special get method which reads keys with prefix or else falls back to key without prefix
     * SearchOrder ["prefixName.prefixSubName.key", "prefixClassName.prefixSubName.key", "root.prefixSubName.key", "prefixSubName.key", "key"]
     *
     * @param key             to look for
     * @param prefixName      [Optional] e.g. logger name
     * @param prefixClassName [Optional] e.g. logger simpleClassName
     * @param prefixSubName   [Optional] e.g. writer/formatter name
     * @return {@link Optional#empty()} if no key combination has a value
     */
    public Optional<String> get(final String key, final String prefixName, final String prefixClassName, final String prefixSubName) {
        final String sub = isEmpty(prefixSubName) ? "" : prefixSubName.toLowerCase() + ".";
        final String lowerCaseKey = key.toLowerCase();
        return Optional.ofNullable(this.get((isEmpty(prefixName) ? "#" : prefixName.toLowerCase() + ".") + sub + lowerCaseKey))
                .or(() -> Optional.ofNullable(this.get((isEmpty(prefixClassName) ? "#" : prefixClassName.toLowerCase() + ".") + sub + lowerCaseKey)))
                .or(() -> Optional.ofNullable(this.get("root." + sub + lowerCaseKey)))
                .or(() -> Optional.ofNullable(this.get(sub + lowerCaseKey)))
                .or(() -> Optional.ofNullable(this.get(lowerCaseKey)));
    }

    /**
     * adds or updates config {@link Properties} from {@link InputStream}
     *
     * @param inputStream containing {@link Properties}
     */
    public void put(final InputStream inputStream) {
        final Properties prop = new Properties();
        try {
            prop.load(inputStream);
            put(prop);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            inputStream.close();
        } catch (Exception ignored) {
            //ignored
        }
    }

    /**
     * adds or updates config values from {@link Properties}
     *
     * @param properties config values
     */
    public void put(final Properties properties) {
        properties.stringPropertyNames().forEach(key -> {
            if (key.toLowerCase().startsWith(LOGGER_PREFIX) && !key.toLowerCase().contains("password")) {
                this.put(key, properties.getProperty(key));
            }
        });
    }

    /**
     * adds or updates config {@link Properties} from absolute file path
     *
     * @param path file wich contains {@link Properties}
     */
    public void put(final Path path) {
        try {
            put(Files.newInputStream(path));
        } catch (IOException e) {
            getLogger(LOGGER_PREFIX + "file").severe("Unable to read property file [" + path.toUri() + "] with value [" + e.getMessage() + "]");
        }
    }

    /**
     * adds or updates config {@link Properties} from filePaths {@link String}
     *
     * @param filePaths containing {@link Properties} files - read details at {@link LoggerConfigLoader#get(String, String, String, String)}
     */
    public LoggerConfigLoader putPaths(final String... filePaths) {
        for (String fileName : filePaths) {
            if (fileName != null && !fileName.isBlank()) {
                Optional.ofNullable(this.getClass().getClassLoader().getResourceAsStream(fileName)).ifPresent(this::put);
                Stream.of(
                        Path.of(System.getProperty("user.dir"), fileName),
                        Path.of(System.getProperty("user.dir"), "config", fileName),
                        Path.of("config", fileName),
                        Path.of(fileName)
                ).filter(Files::exists).filter(Files::isRegularFile).forEach(this::put);
            }
        }
        return this;
    }

    @Override
    public String put(final String key, final String value) {
        final String lowerCaseKey = key.toLowerCase();
        return super.put(
                lowerCaseKey.startsWith(LOGGER_PREFIX) ? key.substring(LOGGER_PREFIX.length()) : lowerCaseKey,
                removeQuotes(value)
        );
    }

    @Override
    public void putAll(final Map<? extends String, ? extends String> config) {
        config.forEach(this::put);
    }

    private LoggerConfigLoader() {
        readEnvs().putPaths("application.properties", "logger.properties").putPaths(get("file"));
    }

    private LoggerConfigLoader readEnvs() {
        put(System.getProperties());
        return this;
    }

    public static boolean isEmpty(final String string) {
        return string == null || string.trim().length() <= 0;
    }

    public static String removeQuotes(final String string) {
        if ((string.startsWith("\"") && string.endsWith("\"")) ||
                (string.startsWith("'") && string.endsWith("'"))) {
            return string.substring(1, string.length() - 1);
        }
        return string;
    }

}
