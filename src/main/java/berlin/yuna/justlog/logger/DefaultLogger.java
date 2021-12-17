package berlin.yuna.justlog.logger;

public class DefaultLogger extends Logger {

    public static Logger instance() {
        return add(new DefaultLogger(getCaller().getClassName())).initDefaults();
    }

    public static Logger instance(final String name) {
        return add(new DefaultLogger(name)).initDefaults();
    }

    public static Logger instance(final Class<?> clazz) {
        return add(new DefaultLogger(clazz)).initDefaults();
    }

    public DefaultLogger(final String name) {
        super(name);
    }

    public DefaultLogger(final Class<?> clazz) {
        super(clazz);
    }
}
