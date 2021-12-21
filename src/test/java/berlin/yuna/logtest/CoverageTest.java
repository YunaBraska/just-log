package berlin.yuna.logtest;

import berlin.yuna.justlog.config.LoggerConfig;
import berlin.yuna.justlog.logger.Logger;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class CoverageTest extends LoggerTestBase {

    @Test
    void equals_hashCode_toString() {
        final LoggerConfig loggerConfig = new LoggerConfig(Logger.defaultLogger());
        assertThat(loggerConfig.equals(loggerConfig), is(true));
        assertThat(loggerConfig.equals(new LoggerConfig(Logger.defaultLogger())), is(false));
        assertThat(loggerConfig.hashCode(), is(notNullValue()));
        assertThat(loggerConfig.toString(), is(notNullValue()));
    }

}
