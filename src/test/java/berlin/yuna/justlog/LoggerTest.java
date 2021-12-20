package berlin.yuna.justlog;

import berlin.yuna.justlog.config.LoggerConfigLoader;
import berlin.yuna.justlog.formatter.LogFormatter;
import berlin.yuna.justlog.formatter.SimpleLogFormatter;
import berlin.yuna.justlog.logger.DefaultLogger;
import berlin.yuna.justlog.logger.Logger;
import berlin.yuna.justlog.model.LogLevel;
import berlin.yuna.justlog.writer.SimpleWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class LoggerTest {

    @BeforeEach
    void setUp() {
        LoggerConfigLoader.instance().clear();
    }

    @Test
    void testLoggerName() {
        assertLogger(Logger.defaultLogger());
        assertLogger(DefaultLogger.instance());
        assertLogger(DefaultLogger.instance(LoggerTest.class));
        assertThat(DefaultLogger.instance(LoggerTest.class.getSimpleName()).name(), is(equalTo(LoggerTest.class.getSimpleName())));
        assertThat(DefaultLogger.instance("CustomName").name(), is(equalTo("CustomName")));
    }

    @Test
    void testFormatter() {
        Logger.defaultLogger().info(() -> "message", new RuntimeException("TestException"));
        final LogFormatter formatter = Logger.defaultLogger().formatter().pattern("[%p{l=10}] [%l{l=5}] [%d{p=HH:mm:ss.SSS}] [%c{l=10}] [%T{l=10,i=0}:%M:%L] %m%n%e{p=berlin,yuna}");
        System.out.println(formatter.format(LogLevel.INFO, "message", new RuntimeException("TestException")));
    }

    private void assertLogger(final Logger logger) {
        assertThat(logger.name(), is(equalTo(LoggerTest.class.getCanonicalName())));
        assertThat(logger.level(), is(equalTo(LogLevel.INFO)));
        assertThat(logger.configLoader(), is(notNullValue()));
        assertThat(logger.formatter().getClass(), is(equalTo(SimpleLogFormatter.class)));
        assertThat(logger.formatter().logger(), is(equalTo(logger)));
        assertThat(logger.writer().getClass(), is(equalTo(SimpleWriter.class)));
        assertThat(logger.writer().logger(), is(equalTo(logger)));
        assertThat(((SimpleWriter) logger.writer()).encoding(), is(equalTo(US_ASCII)));
        assertThat(((SimpleWriter) logger.writer()).bufferSize(), is(100));
    }
}