# Just Logger
Simple Java only logger without reflection or other magic

[![Build][build_shield]][build_link]
[![Maintainable][maintainable_shield]][maintainable_link]
[![Coverage][coverage_shield]][coverage_link]
[![Issues][issues_shield]][issues_link]
[![Commit][commit_shield]][commit_link]
[![Dependencies][dependency_shield]][dependency_link]
[![License][license_shield]][license_link]
[![Central][central_shield]][central_link]
[![Tag][tag_shield]][tag_link]
[![Javadoc][javadoc_shield]][javadoc_link]
[![Size][size_shield]][size_shield]
![Label][label_shield]

[build_shield]: https://github.com/YunaBraska/just-log/workflows/JAVA_CI/badge.svg
[build_link]: https://github.com/YunaBraska/just-log/actions?query=workflow%3AJAVA_CI
[maintainable_shield]: https://img.shields.io/codeclimate/maintainability/YunaBraska/just-log?style=flat-square
[maintainable_link]: https://codeclimate.com/github/YunaBraska/just-log/maintainability
[coverage_shield]: https://img.shields.io/codeclimate/coverage/YunaBraska/just-log?style=flat-square
[coverage_link]: https://codeclimate.com/github/YunaBraska/just-log/test_coverage
[issues_shield]: https://img.shields.io/github/issues/YunaBraska/just-log?style=flat-square
[issues_link]: https://github.com/YunaBraska/just-log/commits/main
[commit_shield]: https://img.shields.io/github/last-commit/YunaBraska/just-log?style=flat-square
[commit_link]: https://github.com/YunaBraska/just-log/issues
[license_shield]: https://img.shields.io/github/license/YunaBraska/just-log?style=flat-square
[license_link]: https://github.com/YunaBraska/just-log/blob/main/LICENSE
[dependency_shield]: https://img.shields.io/librariesio/github/YunaBraska/just-log?style=flat-square
[dependency_link]: https://libraries.io/github/YunaBraska/just-log
[central_shield]: https://img.shields.io/maven-central/v/berlin.yuna/just-log?style=flat-square
[central_link]:https://search.maven.org/artifact/berlin.yuna/just-log
[tag_shield]: https://img.shields.io/github/v/tag/YunaBraska/just-log?style=flat-square
[tag_link]: https://github.com/YunaBraska/just-log/releases
[javadoc_shield]: https://javadoc.io/badge2/berlin.yuna/just-log/javadoc.svg?style=flat-square
[javadoc_link]: https://javadoc.io/doc/berlin.yuna/just-log
[size_shield]: https://img.shields.io/github/repo-size/YunaBraska/just-log?style=flat-square
[label_shield]: https://img.shields.io/badge/Yuna-QueenInside-blueviolet?style=flat-square
[gitter_shield]: https://img.shields.io/gitter/room/YunaBraska/just-log?style=flat-square
[gitter_link]: https://gitter.im/just-log/Lobby

### Motivation

* Logger with lazy messages
* Logger without reflections
* Logger which can run natively (GraalVM)
* Logger which is customizable
* Logger without huge dependency tree
* Logger simple to use (Always hard to find out how to set up SLF4J - afterwards it's awesome)

### Function

[Config](https://github.com/YunaBraska/just-log/tree/main/src/main/java/berlin/yuna/justlog/config)
-> [Logger](https://github.com/YunaBraska/just-log/tree/main/src/main/java/berlin/yuna/justlog/logger) ([LogLevel](https://github.com/YunaBraska/just-log/blob/main/src/main/java/berlin/yuna/justlog/model/LogLevel.java))
-> [Formatter](https://github.com/YunaBraska/just-log/tree/main/src/main/java/berlin/yuna/justlog/formatter) ([LogLevel](https://github.com/YunaBraska/just-log/tree/main/src/main/java/berlin/yuna/justlog/provider))
-> [Writer](https://github.com/YunaBraska/just-log/tree/main/src/main/java/berlin/yuna/justlog/writer)

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

![logo](src/test/resources/logo.png)
