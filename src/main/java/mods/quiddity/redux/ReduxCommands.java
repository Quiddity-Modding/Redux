package mods.quiddity.redux;

import com.google.common.collect.ImmutableList;
import mods.quiddity.redux.json.model.Trigger;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that handles all of the extra commands that we add for use with the command scripting
 *
 * @author winsock on 2/6/15.
 */
public class ReduxCommands {

    public static final ReduxCommand TEST_FOR_TRIGGER = new ReduxCommand("testfortrigger", "/testfortrigger <trigger type>", new ReduxCommandRunnable() {
        @Override
        public void run(ICommandSender sender, String... args) throws CommandException {
            if (args.length < 1)
                throw new CommandException("Incorrect parameters");
            String trigger = args[0];
            try {
                Trigger.TriggerEvent event = Trigger.TriggerEvent.valueOf(trigger);
                if (event != null) {
                    if (sender instanceof ReduxCommandBlockTileEntity.ReduxBlockEventReceiver) {
                        ReduxCommandBlockTileEntity.ReduxBlockEventReceiver reduxBlockEventReceiver = (ReduxCommandBlockTileEntity.ReduxBlockEventReceiver) sender;

                        if (Trigger.TriggerEvent.getTriggerEventFromForgeEvent(reduxBlockEventReceiver.getLastEvent().getClass()) == event) {
                            sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, 1);
                            return;
                        }
                    } else {
                        throw new CommandException("This command is only useful in Redux Pack Blocks");
                    }
                }
            } catch (IllegalArgumentException ignored) {
                throw new CommandException("Trigger type %s does not exist!", trigger);
            }
            sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, 0);
        }
    });

    public static final ReduxCommand TEST_FOR_PROPERTY = new ReduxCommand("testforproperty", "/testforproperty <property name> <integer value>", new ReduxCommandRunnable() {
        @Override
        public void run(ICommandSender sender, String... args) throws CommandException {
            if (args.length < 2)
                throw new CommandException("Incorrect parameters");
            int testValue;
            try {
                testValue = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                throw new CommandException("Second parameter must be an integer!");
            }

            if (sender instanceof ReduxCommandBlockTileEntity.ReduxBlockEventReceiver) {
                ReduxCommandBlockTileEntity.ReduxBlockEventReceiver reduxBlockEventReceiver = (ReduxCommandBlockTileEntity.ReduxBlockEventReceiver) sender;
                IBlockState blockState = sender.getEntityWorld().getBlockState(reduxBlockEventReceiver.getPosition());
                if (blockState.getBlock() instanceof ReduxBlock) {
                    PropertyInteger property = ((ReduxBlock) blockState.getBlock()).getPropertyFromName(args[0]);
                    if (property != null && blockState.getValue(property) != null && blockState.getValue(property).equals(testValue)) {
                        sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, ((Integer) blockState.getValue(property)) > 15 ? 15 : ((Integer) blockState.getValue(property)));
                    }
                } else {
                    throw new CommandException("This command is only useful in Redux Pack Blocks");
                }
            } else {
                throw new CommandException("This command is only useful in Redux Pack Blocks");
            }
            sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, 0);
        }
    });

    public static final ReduxCommand SET_PROPERTY = new ReduxCommand("setproperty", "/setproperty <property name> <integer value>", new ReduxCommandRunnable() {
        @Override
        public void run(ICommandSender sender, String... args) throws CommandException {
            if (args.length < 2)
                throw new CommandException("Incorrect parameters");
            int setValue;
            try {
                setValue = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                throw new CommandException("Second parameter must be an integer!");
            }

            if (sender instanceof ReduxCommandBlockTileEntity.ReduxBlockEventReceiver) {
                ReduxCommandBlockTileEntity.ReduxBlockEventReceiver reduxBlockEventReceiver = (ReduxCommandBlockTileEntity.ReduxBlockEventReceiver) sender;
                IBlockState blockState = sender.getEntityWorld().getBlockState(reduxBlockEventReceiver.getPosition());
                if (blockState.getBlock() instanceof ReduxBlock) {
                    PropertyInteger property = ((ReduxBlock) blockState.getBlock()).getPropertyFromName(args[0]);
                    if (property != null && blockState.getValue(property) != null) {
                        reduxBlockEventReceiver.getEntityWorld().setBlockState(reduxBlockEventReceiver.getPosition(), blockState.withProperty(property, setValue));
                        sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, 1);
                    }
                } else {
                    throw new CommandException("This command is only useful in Redux Pack Blocks");
                }
            } else {
                throw new CommandException("This command is only useful in Redux Pack Blocks");
            }
            sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, 0);
        }
    });

    private static final List<ICommand> commands = new ArrayList<ICommand>();

    static {
        commands.add(TEST_FOR_TRIGGER);
        commands.add(TEST_FOR_PROPERTY);
        commands.add(SET_PROPERTY);
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
