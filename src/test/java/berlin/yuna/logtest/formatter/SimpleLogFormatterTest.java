package berlin.yuna.logtest.formatter;


import berlin.yuna.logtest.LoggerTestBase;
import berlin.yuna.justlog.formatter.LogFormatter;
import berlin.yuna.justlog.formatter.SimpleLogFormatter;
import berlin.yuna.justlog.logger.Logger;
import berlin.yuna.justlog.model.LogLevel;
import berlin.yuna.justlog.provider.EmptyProvider;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Tag("UnitTest")
class SimpleLogFormatterTest extends LoggerTestBase {

    private Logger logger;
    private SimpleLogFormatter formatter;

    @BeforeEach
    void setUp() {
        logger = Logger.defaultLogger();
        formatter = (SimpleLogFormatter) logger.formatter();
    }

    @Test
    void formatTest() {
        assertThat(formatter.pattern(null).patternCompiled().size(), is(0));
        assertThat(formatter.format(LogLevel.INFO, "MyMessage", null), is(equalTo("MyMessage" + System.lineSeparator())));
        assertThat(formatter.formatJson(LogLevel.INFO, "MyMessage", null), is(equalTo("{\"message\":\"MyMessage\"}")));

        assertThat(formatter.pattern("").patternCompiled().size(), is(0));
        assertThat(formatter.formatJson(LogLevel.INFO, "MyMessage", null), is(equalTo("{\"message\":\"MyMessage\"}")));

        assertThat(formatter.pattern("[%l{l=5}] [%d{p=HH:mm:ss},z=UTC] [%c{l=10}] %m%n%e").patternCompiled().size(), is(7));
        assertThat(formatter.pattern("[%l{l=5}] [%c{l=10}] %m%n%e{p=berlin.yuna}").patternCompiled().size(), is(6));

        assertThat(formatter.format(LogLevel.INFO, "MyMessage", new RuntimeException("MyException")), containsString("[INFO ] [b.y.l.f.SimpleLogFormatterTest] MyMessage" + System.lineSeparator() + formattedException()));
        assertThat(formatter.formatJson(LogLevel.INFO, "MyMessage", new RuntimeException("MyException")).startsWith("{\"logLevel\":\"INFO \",\"loggerName\":\"b.y.l.f.SimpleLogFormatterTest\",\"message\":\"MyMessage\",\"exception\":\"java.lang.RuntimeException: MyException\\\\n\\\\tat berlin.yuna.logtest.formatter.SimpleLogFormatterTest.formatTest(SimpleLogFormatterTest.java:"), is(true));

        MatcherAssert.assertThat(LogFormatter.getProviders().size(), is(12));
        LogFormatter.addProvider(new EmptyProvider());
        assertThat(LogFormatter.getProviders().size(), is(13));
        LogFormatter.addProvider(new EmptyProvider());
        assertThat(LogFormatter.getProviders().size(), is(13));
    }

    private String formattedException() {
        return "java.lang.RuntimeException: MyException"
                + System.lineSeparator()
                + "\tat "
                + SimpleLogFormatterTest.class.getCanonicalName()
                + ".formatTest(SimpleLogFormatterTest.java";
    }
}
