package mods.quiddity.redux;

import com.google.common.collect.ImmutableList;
import mods.quiddity.redux.json.model.Trigger;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.*;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
                            return;
                        }
                    } else {
                        throw new CommandException("This command is only useful in Redux Pack Blocks");
                    }
                }
            } catch (IllegalArgumentException ignored) {
                throw new CommandException("Trigger type %s does not exist!", trigger);
            }
            throw new CommandException("failure"); // I HATE YOU MOJANG!!!! DON'T USE EXCEPTIONS AS LOGIC!!!
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
                    if (property == null || blockState.getValue(property) == null || !blockState.getValue(property).equals(testValue)) {
                        throw new CommandException("failure"); // I HATE YOU MOJANG!!!! DON'T USE EXCEPTIONS AS LOGIC!!!
                    }
                } else {
                    throw new CommandException("This command is only useful in Redux Pack Blocks");
                }
            } else {
                throw new CommandException("This command is only useful in Redux Pack Blocks");
            }
            throw new CommandException("failure"); // I HATE YOU MOJANG!!!! DON'T USE EXCEPTIONS AS LOGIC!!!
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
                        return;
                    }
                } else {
                    throw new CommandException("This command is only useful in Redux Pack Blocks");
                }
            } else {
                throw new CommandException("This command is only useful in Redux Pack Blocks");
            }
            throw new CommandException("failure"); // I HATE YOU MOJANG!!!! DON'T USE EXCEPTIONS AS LOGIC!!!
        }
    });

    public static final ReduxCommand TEST = new ReduxCommand("test", "/test <value(string or integer) 1> <operation> <value(string or integer) 2>", new ReduxCommandRunnable() {
        @Override
        public void run(ICommandSender sender, String... args) throws CommandException {
            if (args.length < 3)
                throw new CommandException("Incorrect parameters");
            int result = 0;
            try {
                if (args[1].equalsIgnoreCase("&&")) {
                    result = (Boolean.valueOf(args[0]) && Boolean.valueOf(args[2])) ? 1 : 0;
                } else if (args[1].equalsIgnoreCase("||")) {
                    result = (Boolean.valueOf(args[0]) || Boolean.valueOf(args[2])) ? 1 : 0;
                } else if (args[1].equalsIgnoreCase("==")) {
                    result = (Integer.valueOf(args[0]).equals(Integer.valueOf(args[2]))) ? 1 : 0;
                } else if (args[1].equalsIgnoreCase("!=")) {
                    result = (!Integer.valueOf(args[0]).equals(Integer.valueOf(args[2]))) ? 1 : 0;
                } else if (args[1].equalsIgnoreCase("<")) {
                    result = (Integer.valueOf(args[0]).compareTo(Integer.valueOf(args[2])) > 0) ? 1 : 0;
                } else if (args[1].equalsIgnoreCase(">")) {
                    result = (Integer.valueOf(args[0]).compareTo(Integer.valueOf(args[2])) < 0) ? 1 : 0;
                } else if (args[1].equalsIgnoreCase("<=")) {
                    result = (Integer.valueOf(args[0]).compareTo(Integer.valueOf(args[2])) >= 0) ? 1 : 0;
                } else if (args[1].equalsIgnoreCase("=>")) {
                    result = (Integer.valueOf(args[0]).compareTo(Integer.valueOf(args[2])) <= 0) ? 1 : 0;
                } else if (args[1].equalsIgnoreCase("&")) {
                    result = Integer.valueOf(args[0]) & Integer.valueOf(args[2]);
                } else if (args[1].equalsIgnoreCase("|")) {
                    result = Integer.valueOf(args[0]) | Integer.valueOf(args[2]);
                } if (args[1].equalsIgnoreCase("^")) {
                    result = Integer.valueOf(args[0]) ^ Integer.valueOf(args[2]);
                }
            } catch (NumberFormatException e) {
                if (args[1].equalsIgnoreCase("==")) {
                    result = args[0].equalsIgnoreCase(args[2]) ? 1 : 0;
                } else {
                    result = args[0].equalsIgnoreCase(args[2]) ? 0 : 1;
                }
            }
            sender.addChatMessage(new ChatComponentText(String.valueOf(result)));
            if (sender instanceof ReduxCommandBlockTileEntity.ReduxBlockEventReceiver) {
                ReduxCommandBlockTileEntity.ReduxBlockEventReceiver eventReceiver = (ReduxCommandBlockTileEntity.ReduxBlockEventReceiver) sender;
                eventReceiver.setLastTest(result);
            }
            if (result == 0)
                throw new CommandException("failure"); // I HATE YOU MOJANG!!!! DON'T USE EXCEPTIONS AS LOGIC!!!
        }
    });

    public static final ReduxCommand RANDOM = new ReduxCommand("random", "/random <lower> <upper>", new ReduxCommandRunnable() {
        private final Random random = new Random();
        @Override
        public void run(ICommandSender sender, String... args) throws CommandException {
            if (args.length < 2)
                throw new CommandException("Incorrect parameters");
            try {
                int lower = Integer.parseInt(args[0]);
                int upper = Integer.parseInt(args[1]);
                int rand = lower + random.nextInt(upper - lower + 1);
                sender.addChatMessage(new ChatComponentText(String.valueOf(rand)));
            } catch (NumberFormatException e) {
                throw new CommandException("First and Second parameters must be integers!");
            }
        }
    });

    private static final List<ICommand> commands = new ArrayList<ICommand>();

    static {
        commands.add(TEST_FOR_TRIGGER);
        commands.add(TEST_FOR_PROPERTY);
        commands.add(SET_PROPERTY);
        commands.add(TEST);
        commands.add(RANDOM);
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
        public int getRequiredPermissionLevel() {
            return 2;
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
