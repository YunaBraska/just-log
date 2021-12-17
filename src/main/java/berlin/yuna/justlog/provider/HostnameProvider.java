package berlin.yuna.justlog.provider;

import berlin.yuna.justlog.logger.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class HostnameProvider extends Provider {

    private int length;
    private String hostName;

    public HostnameProvider() {
        this.name = 'h';
    }

    @Override
    public Provider compile(final Logger logger, final Map<Character, String> config) {
        this.length = getLength(config);
        this.hostName = getHostname();
        return this;
    }

    @Override
    public String execute(
            final Supplier<String> message,
            final Supplier<Throwable> throwable,
            final Supplier<HashMap<String, String>> params
    ) {
        return hostName;
    }

    @Override
    public Provider refresh(final Supplier<Logger> logger) {
        this.hostName = getHostname();
        return this;
    }

    private String getHostname() {
        try {
            return spaceUp(InetAddress.getLocalHost().getHostName(), length);
        } catch (UnknownHostException e) {
            return spaceUp("0.0.0.0", length);
        }
    }
}
