package mods.quiddity.redux;

import mods.quiddity.redux.json.model.Flags;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.spi.AbstractLogger;

/**
 * This is the main Redux logger.
 *
 * @author winsock on 2/7/15.
 */
public class ReduxLogger extends AbstractLogger {

    private final String prefix;
    private int maxLogLevel = 3;

    public static final String LOG_LEVEL_CONFIG_KEY = "log_level";

    public ReduxLogger(String modName) {
        this.prefix = modName;
    }

    public void loadConfigLevel() {
        if (Redux.instance.getReduxConfiguration() != null) {
            Flags<String, ?> logLevel = Redux.instance.getReduxConfiguration().getFlagForName(LOG_LEVEL_CONFIG_KEY, new Flags<String, Integer>(LOG_LEVEL_CONFIG_KEY, 3));
            if (logLevel != null && logLevel.getValue() instanceof Integer) {
                maxLogLevel = (Integer) logLevel.getValue();
            } else {
                maxLogLevel = Level.WARN.intLevel();
            }
        }
    }

    @Override
    protected boolean isEnabled(Level level, Marker marker, Message data, Throwable t) {
        return level.lessOrEqual(maxLogLevel);
    }

    @Override
    protected boolean isEnabled(Level level, Marker marker, Object data, Throwable t) {
        return level.lessOrEqual(maxLogLevel);
    }

    @Override
    protected boolean isEnabled(Level level, Marker marker, String data) {
        return level.lessOrEqual(maxLogLevel);
    }

    @Override
    protected boolean isEnabled(Level level, Marker marker, String data, Object... p1) {
        return level.lessOrEqual(maxLogLevel);
    }

    @Override
    protected boolean isEnabled(Level level, Marker marker, String data, Throwable t) {
        return level.lessOrEqual(maxLogLevel);
    }

    @Override
    public void log(Marker marker, String fqcn, Level level, Message data, Throwable t) {
        if (t != null) {
            t.printStackTrace(System.out.format("[%s] %s: %s. Throwable Exception:\n", level.toString(), prefix, data.getFormattedMessage()));
        } else {
            System.out.format("[%s] %s: %s. Caller: %s\n", level.toString(), prefix, data.getFormattedMessage(), fqcn);
        }
    }
}
