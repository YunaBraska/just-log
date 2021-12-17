# Just Logger

### Motivation

* Logger with lazy messages
* Logger without reflections
* Logger which can run natively (GraalVM)
* Logger which is customizable
* Logger without huge dependency tree
* Logger simple to use (Always hard to find out how to set up SLF4J - afterwards it's awesome)

### Usage

* Default

```Java
import berlin.yuna.justlog.logger.Logger;

public class UsageTest {

    private static final Logger logger1 = DefaultLogger.instance();
    private static final Logger logger2 = Logger.defaultLogger();

    instance();

    public void log() {
        logger1.warn(() -> "message", () -> new RuntimeException("exception"));
        logger2.warn(() -> "message", () -> new RuntimeException("exception"));
    }
}
```

* DSL

```Java
public class UsageTest {

    private final Logger logger1;
    private final Logger logger2;

    public UsageTest {
        final HashMap<String, String> myOptionalConfig = new HashMap<>();

        logger1 = new DefaultLogger("LoggerName")
                .level(LogLevel.TRACE)
                .writer(new SimpleWriter().config(myOptionalConfig))
                .formatter(new SimpleLogFormatter().config(myOptionalConfig))
                .formatter().pattern("[%p{l=10}] [%l{l=5}] [%d{p=HH:mm:ss.SSS}] [%c{l=10}] [%T{l=10,i=0}:%M:%L] %m%n%e{p=berlin,yuna}").logger();

        logger2 = DefaultLogger.instance().level(LogLevel.TRACE).foormatter().config(myOptionalConfig).logger();
    }
}
```

### TODOs

* [ ] Read config from file
* [ ] Read config from env
* [ ] Read config from dsl
* [ ] Think about Registry for Logger, Formatter, Writer and Provider
* [ ] Split into Java version 8,11,17
