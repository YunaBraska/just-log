package berlin.yuna.logtest.logger;


import berlin.yuna.logtest.LoggerTestBase;
import berlin.yuna.justlog.logger.DefaultLogger;
import berlin.yuna.justlog.logger.Logger;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static berlin.yuna.justlog.model.LogLevel.FATAL;
import static berlin.yuna.justlog.model.LogLevel.INFO;
import static berlin.yuna.justlog.model.LogLevel.OFF;
import static berlin.yuna.justlog.model.LogLevel.WARN;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class DefaultLoggerTest extends LoggerTestBase {

    @Test
    void testLoggerName() {
        assertDefaultLogger(Logger.defaultLogger(), DefaultLoggerTest.class);
        assertDefaultLogger(DefaultLogger.instance(), DefaultLoggerTest.class);
        assertDefaultLogger(DefaultLogger.instance(DefaultLoggerTest.class), DefaultLoggerTest.class);
        assertThat(DefaultLogger.instance(DefaultLoggerTest.class.getSimpleName()).name(), is(equalTo(DefaultLoggerTest.class.getSimpleName())));
        assertThat(DefaultLogger.instance("CustomName").name(), is(equalTo("CustomName")));
        assertThat(Logger.defaultLogger("CustomName").name(), is(equalTo("CustomName")));
    }

    @Test
    void logNothing() {
        final Logger logger = Logger.defaultLogger().level(OFF);
        logger.trace(() -> "TraceMessage");
        logger.debug(() -> "DebugMessage");
        logger.info(() -> "InfoMessage");
        logger.warn(() -> "WarnMessage");
        logger.error(() -> "ErrorMessage");
        logger.fatal(() -> "FatalMessage");
        waitForLog();
    }

    @Test
    void logEverything() {
        final Logger logger = Logger.defaultLogger().config(Map.of("buffer-size", "1")).level(FATAL);
        logger.trace(() -> "TraceMessage");
        logger.trace(() -> "TraceMessage", () -> new RuntimeException("TestException"));
        logger.debug(() -> "DebugMessage");
        logger.debug(() -> "DebugMessage", () -> new RuntimeException("TestException"));
        logger.info(() -> "InfoMessage");
        logger.info(() -> "InfoMessage", () -> new RuntimeException("TestException"));
        logger.warn(() -> "WarnMessage");
        logger.warn(() -> "WarnMessage", () -> new RuntimeException("TestException"));
        logger.error(() -> "ErrorMessage");
        logger.error(() -> "ErrorMessage", () -> new RuntimeException("TestException"));
        logger.fatal(() -> "FatalMessage");
        logger.fatal(() -> "FatalMessage", () -> new RuntimeException("TestException"));
        waitForLog();
    }

    @Test
    void logFullPattern() {
        final Logger logger = Logger.defaultLogger().level(INFO);
        logger.formatter().pattern("%l{l=5} %p{l=5} %d{l=10} %h{l=10} %a{l=10} %c{l=10} %T{i=2}:%M:%L %m%n%e");
        logger.info(() -> "InfoMessage", () -> new RuntimeException("TestException"));
        waitForLog();
    }

    @Test
    void setLogLevel() {
        final Logger logger = Logger.defaultLogger();
        assertThat(logger.level(), is(equalTo(INFO)));
        logger.level(WARN);
        assertThat(logger.level(), is(equalTo(WARN)));
    }

    @Test
    void setConfig() {
        final Logger logger = Logger.defaultLogger();
        assertThat(logger.level(), is(equalTo(INFO)));
        logger.config(Map.of("level", "warn"));
        assertThat(logger.level(), is(equalTo(WARN)));
    }
}
