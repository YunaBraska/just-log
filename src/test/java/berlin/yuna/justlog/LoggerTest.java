package berlin.yuna.justlog;

import berlin.yuna.justlog.formatter.LogFormatter;
import berlin.yuna.justlog.formatter.SimpleLogFormatterOld;
import berlin.yuna.justlog.logger.DefaultLogger;
import berlin.yuna.justlog.logger.Logger;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class LoggerTest {

    @Test
    void testLoggerName() {
        final Logger loggerDefault = Logger.defaultLogger();
        final Logger loggerDefaultInstance = DefaultLogger.instance();
        final Logger loggerDefaultNewClass = DefaultLogger.instance(LoggerTest.class);
        final Logger loggerDefaultNewName = DefaultLogger.instance(LoggerTest.class.getSimpleName());

        assertLogger(loggerDefault);
        assertLogger(loggerDefaultInstance);
        assertLogger(loggerDefaultNewClass);
        assertThat(loggerDefaultNewName.name(), is(equalTo(LoggerTest.class.getSimpleName())));
    }

    @Test
    void testFormatter() {
        Logger.defaultLogger().info(() -> "message", new RuntimeException("TestException"));
        final LogFormatter formatter = Logger.defaultLogger().formatter().pattern("[%p{l=10}] [%l{l=5}] [%d{p=HH:mm:ss.SSS}] [%c{l=10}] [%T{l=10,i=0}:%M:%L] %m%n%e{p=berlin,yuna}");
        System.out.println(formatter.format(LogLevel.INFO, "message", new RuntimeException("TestException")));
    }

    private void assertLogger(final Logger logger) {
        assertThat(logger.name(), is(equalTo(LoggerTest.class.getCanonicalName())));
        assertThat(logger.formatter().getClass(), is(equalTo(SimpleLogFormatterOld.class)));
    }
}