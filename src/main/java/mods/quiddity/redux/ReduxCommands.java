package mods.quiddity.redux;

import com.google.common.collect.ImmutableList;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that handles all of the extra commands that we add for use with the command scripting
 *
 * @author winsock on 2/6/15.
 */
public class ReduxCommands {

    public static final ReduxCommand DEBUG = new ReduxCommand("redux-debug", "/redux-debug", new ReduxCommandRunnable() {
        @Override
        public void run(ICommandSender sender, String... args) throws CommandException {

        }
    });

    private static final List<ICommand> commands = new ArrayList<ICommand>();

    static {
        commands.add(DEBUG);
    }

    public static List<ICommand> getCommands() {
        return ImmutableList.copyOf(commands);
    }

    private abstract static class ReduxCommandRunnable {
        public abstract void run(ICommandSender sender, String... args) throws CommandException;
    }

    public static class ReduxCommand extends CommandBase {
        private final String name;
        private final String commandUsage;
        private final ReduxCommandRunnable commandRunnable;

        public ReduxCommand(String name, String usage, ReduxCommandRunnable runnable) {
            this.name = name;
            this.commandUsage = usage;
            this.commandRunnable = runnable;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getCommandUsage(ICommandSender sender) {
            return commandUsage;
        }

        @Override
        public void execute(ICommandSender sender, String[] args) throws CommandException {
            commandRunnable.run(sender, args);
        }
    }
}
