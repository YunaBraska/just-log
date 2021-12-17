package berlin.yuna.justlog.writer;

import berlin.yuna.justlog.logger.Logger;

import java.util.Map;

public abstract class LogWriter {

    protected Logger logger;

    public abstract LogWriter config(final Map<String, String> config);

    public abstract LogWriter logOut(final String msg);

    public abstract LogWriter logError(final String msg);

    public Logger logger() {
        return logger;
    }

    public LogWriter logger(final Logger pLogger) {
        this.logger = pLogger;
        return this;
    }
}
