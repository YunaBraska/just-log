package berlin.yuna.logtest;


import berlin.yuna.justlog.config.LoggerConfigLoader;
import berlin.yuna.justlog.formatter.SimpleLogFormatter;
import berlin.yuna.justlog.logger.Logger;
import berlin.yuna.justlog.model.LogLevel;
import berlin.yuna.justlog.writer.SimpleWriter;
import org.junit.jupiter.api.BeforeEach;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public abstract class LoggerTestBase {

    @BeforeEach
    void setUp() {
        Logger.getAll().forEach(Logger::remove);
        assertThat(Logger.getAll().size(), is(0));
        LoggerConfigLoader.instance().clear();
    }

    protected void waitForLog() {
        while (Logger.isRunning()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void assertDefaultLogger(final Logger logger, final Class<?> executor) {
        assertThat(logger.name(), is(equalTo(executor.getCanonicalName())));
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
