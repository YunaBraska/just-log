package berlin.yuna.justlog.provider;

import berlin.yuna.justlog.logger.Logger;
import berlin.yuna.justlog.model.LogLevel;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AddressProvider extends Provider {

    private int length;
    private String address;

    public AddressProvider() {
        this.id = 'a';
        this.name = "address";
    }

    @Override
    public Provider compile(final Logger logger, final Map<Character, String> config) {
        this.length = getLength(config);
        this.address = getAddress();
        return this;
    }

    @Override
    public String execute(
            final Supplier<LogLevel> level,
            final Supplier<String> message,
            final Supplier<Throwable> throwable,
            final Supplier<HashMap<String, String>> params
    ) {
        return address;
    }

    @Override
    public Provider refresh(final Supplier<Logger> logger) {
        this.address = getAddress();
        return this;
    }

    private String getAddress() {
        try {
            return spaceUp(InetAddress.getLocalHost().getHostAddress(), length);
        } catch (UnknownHostException e) {
            return spaceUp("0.0.0.0", length);
        }
    }
}
