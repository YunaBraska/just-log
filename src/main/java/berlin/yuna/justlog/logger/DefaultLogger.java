package berlin.yuna.justlog.logger;

import java.io.Serial;
import java.io.Serializable;

public class DefaultLogger extends Logger implements Serializable {

    @Serial
    private static final long serialVersionUID = 6087053848055106765L;

    public static Logger instance() {
        return add(new DefaultLogger(getCaller().getClassName()));
    }

    public static Logger instance(final String name) {
        return add(new DefaultLogger(name));
    }

    public static Logger instance(final Class<?> clazz) {
        return add(new DefaultLogger(clazz));
    }

    public DefaultLogger(final String name) {
        super(name);
    }

    public DefaultLogger(final Class<?> clazz) {
        super(clazz);
    }
}
