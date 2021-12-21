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

    private static final Logger logger = Logger.defaultLogger();

    public void log() {
        logger.warn(() -> "message", () -> new RuntimeException("exception"));
    }
}
```

### Configuration

* Available [LogLevel](https://github.com/YunaBraska/just-log/blob/main/src/main/java/berlin/yuna/justlog/model/LogLevel.java)
* Available [ConfigLoader](https://github.com/YunaBraska/just-log/blob/main/src/main/java/berlin/yuna/justlog/config/LoggerConfigLoader.java)
* Configuration priorities **\[Environment Variables, Config Files, DSL\]**
* Configuration folders **\[classpath:resource, userDir, userDir:config\]**
* Configuration files **\[application.properties, logger.properties\]**
* Configuration values are **\[case insensitive]**
    * Every config without `logger.` prefix will be ignored
* Example config

```properties
#Default logger config
logger.file=/user/home/service/config/custom.properties
logger.level=INFO
logger.threads=2
logger.writer.encoding=UTF8
logger.SimpleWriter.buffer-size=10
logger.pattern="[%p{l=10}] [%l{l=5}] [%d{p=HH:mm:ss.SSS}] [%c{l=10}] [%T{l=10,i=0}:%M:%L] %m%n%e{p=berlin,yuna}"
#Default logger config - the keyword root is optional and has no meaning
logger.root.file=/user/home/service/config/custom.properties
#Logger specific config
logger.myLoggerName.file=/user/home/service/config/custom.properties
logger.myLoggerClass.level=ERROR
logger.myLoggerClass.myFormatter.pattern="[[%d{p=HH:mm:ss.SSS}] [%c{l=10}] %m%n%e"
```

### Pattern & Placeholder

* The log pattern defines the format of the log message by using placeholders
    * Placeholders are a simple `chars` with `%` as prefix
    * Placeholders are named `provicers` in code
    * It is also possible to create custom providers, see examples
      of [DefaultProviders](https://github.com/YunaBraska/just-log/tree/main/src/main/java/berlin/yuna/justlog/provider)
* All default Providers:

| Char | Accepts | Name               | Description                                  |
|------|---------|--------------------|----------------------------------------------|
| a    | l       | IP Address         | First IP Address of the current host         |
| c    | l       | logger name        | logger name                                  |
| d    | l, z    | DateTime           | date time using DateTimeFormatter            |
| e    | p       | Exception          | exception                                    |
| h    | l       | Hostname           | Hostname                                     |
| l    | l       | Log Level          | log level for that message                   |
| m    | -       | Message            | actual message                               |
| n    | -       | New Line           | new line                                     |
| p    | l       | PID                | process id of the current thread             |
| T    | l, i    | Caller Class       | Caller class name \[slows the performance\]  |
| M    | l, i    | Caller Method      | Caller Method \[slows the performance\]      |
| L    | l, i    | Caller Line Number | Caller Line Number \[slows the performance\] |

* Every Provider accepts `,` separated parameters within brackets `{}` like on default:

| Char | Name               | Examples                                                       |
|------|--------------------|----------------------------------------------------------------|
| l    | Length             | `%l{l=5}` Loglevel length  <br /> `%c{l=10}` LoggerName length |
| i    | Index              | `%T{i=0}` Caller class index number (0 = default)              |
| p    | Pattern            | `%d{p=HH:mm:ss.SSS}` Date format <br /> `%e{p=berlin;yuna}` include only traces with packages in `berlin` and `yuna`         |
| z    | TimeZone           | `%d{z=UTC}` Sets the time zone (UTC = default)               |

* DSL Config

```Java
public class UsageTest {

    private final Logger logger1;
    private final Logger logger2;

    public UsageTest {
        final HashMap<String, String> myOptionalConfig = new HashMap<>();

        logger1 = new DefaultLogger("LoggerName")
                .config(myOptionalConfig)
                .level(LogLevel.TRACE)
                .writer(new SimpleWriter())
                .formatter(new SimpleLogFormatter())
                .formatter().pattern("[%p{l=10}] [%l{l=5}] [%d{p=HH:mm:ss.SSS}] [%c{l=10}] [%T{l=10,i=0}:%M:%L] %m%n%e{p=berlin,yuna}").logger();

        logger2 = DefaultLogger.instance().config(myOptionalConfig).level(LogLevel.TRACE);
    }
}
```

### TODOs

* [ ] Test the logger in a project
* [ ] String format message
* [ ] Config to enable automatically Json / Line logging
* [ ] FileWriter
* [ ] Accept Multiple Writer
* [ ] Split into Java version 8,11,17
