package berlin.yuna.justlog.config;

import berlin.yuna.justlog.model.LogLevel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Properties;

import static berlin.yuna.justlog.config.LoggerConfigLoader.LOGGER_PREFIX;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class LoggerConfigLoaderTest {

    private static Path configFilePath;

    @BeforeAll
    static void beforeAll() throws URISyntaxException {
        configFilePath = Path.of(LoggerConfigLoaderTest.class.getClassLoader().getResource("customLogger.properties").toURI());
    }

    @BeforeEach
    void setUp() {
        LoggerConfigLoader.instance().clear();
    }

    @Test
    void constructor_Test() {
        final LoggerConfigLoader config = LoggerConfigLoader.instance();
        assertThat(config.size(), is(0));
    }

    @Test
    void put_Path_Test() {
        final LoggerConfigLoader config = LoggerConfigLoader.instance();
        assertThat(config.size(), is(0));
        config.put(configFilePath);
        assertThat(config.size(), is(1));
        assertThat(config.get("level"), is(equalTo(LogLevel.ERROR.name())));
    }

    @Test
    void put_InvalidPath_Test() {
        final LoggerConfigLoader config = LoggerConfigLoader.instance();
        assertThat(config.size(), is(0));
        config.put(Path.of("invalid"));
        assertThat(config.size(), is(0));
    }

    @Test
    void put_InputStream_Test() throws FileNotFoundException {
        final LoggerConfigLoader config = LoggerConfigLoader.instance();
        assertThat(config.size(), is(0));
        config.put(new FileInputStream(configFilePath.toFile()));
        assertThat(config.size(), is(1));
        assertThat(config.get("level"), is(equalTo(LogLevel.ERROR.name())));
    }

    @Test
    void put_InvalidInputStream_Test() {
        final LoggerConfigLoader config = LoggerConfigLoader.instance();
        assertThat(config.size(), is(0));
        config.put((InputStream) null);
        assertThat(config.size(), is(0));
    }

    @Test
    void put_Properties_Test() {
        final LoggerConfigLoader config = LoggerConfigLoader.instance();
        final Properties properties = new Properties();
        properties.put(LOGGER_PREFIX + "level", LogLevel.ERROR.name());

        assertThat(config.size(), is(0));
        config.put(properties);
        assertThat(config.size(), is(1));
        assertThat(config.get("level"), is(equalTo(LogLevel.ERROR.name())));
    }

    @Test
    void put_PathStrings_Test() {
        final LoggerConfigLoader config = LoggerConfigLoader.instance();
        assertThat(config.size(), is(0));
        config.putPaths(configFilePath.toString());
        assertThat(config.size(), is(1));
        assertThat(config.get("level"), is(equalTo(LogLevel.ERROR.name())));
    }

    @Test
    void put_QuotedValues_Test() {
        final LoggerConfigLoader config = LoggerConfigLoader.instance();
        final Properties properties = new Properties();
        properties.put(LOGGER_PREFIX + "level", "\"" + LogLevel.ERROR.name() + "\"");

        assertThat(config.size(), is(0));
        config.put(properties);
        assertThat(config.size(), is(1));
        assertThat(config.get("level"), is(equalTo(LogLevel.ERROR.name())));
    }

    @Test
    void get_Test() {
        final LoggerConfigLoader config = LoggerConfigLoader.instance();

        config.put("LeVeL", "ERROR");
        config.put("customLogger.LeVeL", "WARN");
        config.put("customLogger.CustomFormatter.LeVeL", "INFO");
        assertThat(config.get("level"), is(equalTo(LogLevel.ERROR.name())));
        assertThat(config.get("level", null, null, null).orElse(null), is(equalTo(LogLevel.ERROR.name())));
        assertThat(config.get("level", "root", null, null).orElse(null), is(equalTo(LogLevel.ERROR.name())));
        assertThat(config.get("level", "customlogger", null, null).orElse(null), is(equalTo(LogLevel.WARN.name())));
        assertThat(config.get("level", "customlogger", "customlogger", null).orElse(null), is(equalTo(LogLevel.WARN.name())));
        assertThat(config.get("level", null, "customlogger", null).orElse(null), is(equalTo(LogLevel.WARN.name())));
        assertThat(config.get("level", "customlogger", null, "customformatter").orElse(null), is(equalTo(LogLevel.INFO.name())));
        assertThat(config.get("level", null, "customlogger", "customformatter").orElse(null), is(equalTo(LogLevel.INFO.name())));
    }

}