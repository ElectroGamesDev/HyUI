package au.ellie.hyui.builders;

import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper for UICommandBuilder that logs all .set() calls.
 */
public class LoggingUICommandBuilder extends UICommandBuilder {
    private final List<String> commandLog = new ArrayList<>();

    @Override
    public UICommandBuilder set(String path, String value) {
        commandLog.add("set(" + path + ", " + value + ")");
        return super.set(path, value);
    }

    @Override
    public UICommandBuilder set(String path, boolean value) {
        commandLog.add("set(" + path + ", " + value + ")");
        return super.set(path, value);
    }

    @Override
    public UICommandBuilder set(String path, int value) {
        commandLog.add("set(" + path + ", " + value + ")");
        return super.set(path, value);
    }

    @Override
    public UICommandBuilder set(String path, float value) {
        commandLog.add("set(" + path + ", " + value + ")");
        return super.set(path, value);
    }

    @Override
    public UICommandBuilder set(String path, double value) {
        commandLog.add("set(" + path + ", " + value + ")");
        return super.set(path, value);
    }

    @Override
    public UICommandBuilder set(String path, com.hypixel.hytale.server.core.Message value) {
        commandLog.add("set(" + path + ", " + value + ")");
        return super.set(path, value);
    }

    @Override
    public UICommandBuilder set(String path, Value value) {
        commandLog.add("set(" + path + ", " + value.toString() + ")");
        return super.set(path, value);
    }

    public List<String> getCommandLog() {
        return new ArrayList<>(commandLog);
    }
}
