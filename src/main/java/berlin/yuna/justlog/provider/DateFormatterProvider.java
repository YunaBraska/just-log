package berlin.yuna.justlog.provider;

import berlin.yuna.justlog.logger.Logger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class DateFormatterProvider extends Provider {

    private int length;
    private ZoneId zoneId;
    private DateTimeFormatter dateTimeFormatter;

    public DateFormatterProvider() {
        this.name = 'd';
    }

    @Override
    public Provider compile(final Logger logger, final Map<Character, String> config) {
        this.length = getLength(config);
        this.zoneId = ZoneId.of(getZoneId(config).orElse("UTC"));
        this.dateTimeFormatter = getPattern(config).map(DateTimeFormatter::ofPattern).orElse(DateTimeFormatter.ISO_DATE_TIME);
        return this;
    }

    @Override
    public String execute(
            final Supplier<String> message,
            final Supplier<Throwable> throwable,
            final Supplier<HashMap<String, String>> params
    ) {
        return spaceUp(dateTimeFormatter.format(LocalDateTime.now().atZone(zoneId)), length);
    }


    @Override
    public Provider refresh(final Supplier<Logger> logger) {
        return this;
    }
}
