package mods.quiddity.redux;

import mods.quiddity.redux.json.model.Block;
import mods.quiddity.redux.json.model.Pack;
import mods.quiddity.redux.json.model.Trigger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The tile entity for Redux Pack blocks that have custom command scripts.
 *
 * @author winsock on 2/5/15.
 */
public class ReduxCommandBlockTileEntity extends TileEntity {

    private String packId = "";

    protected volatile Block reduxBlock = null;
    protected int lastSuccessCount = 0;
    protected CommandResultStats.Type lastResultType = CommandResultStats.Type.SUCCESS_COUNT;
    protected int lastResultAmount = 0;

    protected final Set<ReduxBlockEventReceiver> tickEventReceivers = new HashSet<ReduxBlockEventReceiver>();
    protected final Set<ReduxBlockEventReceiver> eventReceivers = new HashSet<ReduxBlockEventReceiver>();
    protected final Map<String, String> reduxVariables = new HashMap<String, String>();

    public ReduxCommandBlockTileEntity() {}

    public int getLastSuccessCount() {
        return lastSuccessCount;
    }
    public void addTickEventReceiver(ReduxBlockEventReceiver receiver) {
        tickEventReceivers.add(receiver);
    }

    public void init(String packId, Block reduxBlock) {
        this.packId = packId;
        this.reduxBlock = reduxBlock;

        for (Trigger trigger : reduxBlock.getScript()) {
            // We have to keep a local strong reference. Otherwise GC would remove our event receiver right away.
            ReduxBlockEventReceiver receiver = new ReduxBlockEventReceiver(trigger);
            eventReceivers.add(receiver);
            ReduxEventDispatcher.getInstance().registerEventReceiver(receiver);
        }
    }

    public void setupTileEntity(String blockId) {
        Pack p = Redux.instance.getReduxConfiguration().getPackFromId(packId);
        if (p == null) throw new AssertionError();
        for (Block b : p.getBlocks()) {
            if (b.getId().equalsIgnoreCase(blockId))
                reduxBlock = b;
        }
        if (reduxBlock == null) throw new AssertionError();

        tickEventReceivers.clear();
        eventReceivers.clear();

        for (Trigger trigger : reduxBlock.getScript()) {
            // We have to keep a local strong reference. Otherwise GC would remove our event receiver right away.
            ReduxBlockEventReceiver receiver = new ReduxBlockEventReceiver(trigger);
            eventReceivers.add(receiver);
            ReduxEventDispatcher.getInstance().registerEventReceiver(receiver);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("lastSuccessCount", lastSuccessCount);
        compound.setInteger(lastResultType.getTypeName(), lastResultAmount);
        compound.setString("pack", packId);
        compound.setString("block", reduxBlock.getId());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.lastSuccessCount = compound.getInteger("lastSuccessCount");
        this.packId = compound.getString("pack");
        setupTileEntity(compound.getString("block"));
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.pos, 0, nbttagcompound);
    }

    protected class ReduxBlockEventReceiver implements ICommandSender {
        private final Trigger triggerScript;
        private int successCount;
        protected Event lastEvent = null;

        public ReduxBlockEventReceiver(Trigger triggerScript) {
            this.triggerScript = triggerScript;

            if (ReduxCommandBlockTileEntity.this instanceof ReduxCommandBlockTickableTileEntity && triggerScript.getTriggerEvent().getForgeEventClass() == Event.class) {
                ReduxCommandBlockTileEntity.this.addTickEventReceiver(this);
            }
        }

        public Trigger getTriggerScript() {
            return triggerScript;
        }

        public Event getLastEvent() {
            return lastEvent;
        }

        @Override
        public String getName() {
            return reduxBlock.getId();
        }

        @Override
        public IChatComponent getDisplayName() {
            return new ChatComponentText(reduxBlock.getName());
        }

        @Override
        public void addChatMessage(IChatComponent message) { }

        @Override
        public boolean canUseCommand(int permLevel, String commandName) {
            return permLevel <= 2;
        }

        @Override
        public BlockPos getPosition() {
            return ReduxCommandBlockTileEntity.this.getPos();
        }

        @Override
        public Vec3 getPositionVector() {
            return new Vec3((double)ReduxCommandBlockTileEntity.this.pos.getX() + 0.5D, (double)ReduxCommandBlockTileEntity.this.pos.getY() + 0.5D, (double)ReduxCommandBlockTileEntity.this.pos.getZ() + 0.5D);
        }

        @Override
        public World getEntityWorld() {
            return ReduxCommandBlockTileEntity.this.getWorld();
        }

        @Override
        public Entity getCommandSenderEntity() {
            return null;
        }

        @Override
        public boolean sendCommandFeedback() {
            return false;
        }

        @Override
        public void setCommandStat(CommandResultStats.Type type, int amount) {
            ReduxCommandBlockTileEntity.this.lastResultType = type;
            ReduxCommandBlockTileEntity.this.lastResultAmount = amount;
        }

        private String getTriggerStringForEvent(Event event) {
            if (Trigger.TriggerEvent.getTriggerEventFromForgeEvent(event.getClass()) != null) {
                if (event instanceof WorldEvent) {
                    WorldEvent worldEvent = (WorldEvent)event;
                    return String.valueOf(worldEvent.world.provider.getDimensionId());
                } else if (event instanceof ChunkWatchEvent) {
                    ChunkWatchEvent chunkWatchEvent = (ChunkWatchEvent) event;
                    return chunkWatchEvent.player.getName();
                }

                switch (Trigger.TriggerEvent.getTriggerEventFromForgeEvent(event.getClass())) {
                    case BlockBreakEvent:
                        BlockEvent.BreakEvent breakEvent = (BlockEvent.BreakEvent) event;
                        return breakEvent.getPlayer().getName();
                    case BlockHarvestDropsEvent:
                        BlockEvent.HarvestDropsEvent harvestDropsEvent = (BlockEvent.HarvestDropsEvent) event;
                        return harvestDropsEvent.harvester.getName();
                    case BlockMultiPlaceEvent:
                    case BlockPlaceEvent:
                        BlockEvent.PlaceEvent placeEvent = (BlockEvent.PlaceEvent)event;
                        return placeEvent.player.getName();
                    case ServerChatEvent:
                        ServerChatEvent serverChatEvent = (ServerChatEvent) event;
                        return serverChatEvent.username;
                    default:
                        return String.valueOf(ReduxCommandBlockTileEntity.this.getWorld().provider.getDimensionId());
                }
            }
            return "";
        }

        public void receiveEvent(Event event) {
            if (worldObj.isRemote)
                return;
            BlockPos blockPos = ReduxCommandBlockTileEntity.this.pos;
            IBlockState defaultState = ReduxCommandBlockTileEntity.this.getWorld().getBlockState(blockPos).getBlock().getDefaultState();
            // Check if the block has changed as result of an event. I.E. BlockBreak
            if (ReduxCommandBlockTileEntity.this.getWorld().getBlockState(pos).getBlock().getClass() != ReduxBlock.class) {
                return;
            }
            reduxVariables.put(Trigger.TriggerEvent.getTriggerEventFromForgeEvent(event.getClass()).name(), getTriggerStringForEvent(event));

            ICommandManager icommandmanager = FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
            Stack<Integer> commandResultStack = new Stack<Integer>();

            lastEvent = event;
            for (String s : triggerScript.getCommands()) {
                String parsedCommand = s;

                Pattern reduxPattern = Pattern.compile("\\$redux\\[[a-zA-Z]+\\]", Pattern.CASE_INSENSITIVE);
                Matcher reduxMatcher = reduxPattern.matcher(parsedCommand);
                Pattern commandPattern = Pattern.compile("\\[[a-zA-Z]+\\]", Pattern.CASE_INSENSITIVE);
                while (reduxMatcher.find() && !reduxMatcher.hitEnd()) {
                    MatchResult result = reduxMatcher.toMatchResult();
                    Matcher commandMatcher = commandPattern.matcher(result.group());
                    if (!commandMatcher.find())
                        continue;
                    String command = commandMatcher.toMatchResult().group().replaceAll("\\[", "").replaceAll("\\]", "");

                    if (command.equalsIgnoreCase("PEEK") || command.equalsIgnoreCase("POP")) {
                        if (command.equalsIgnoreCase("PEEK")) {
                            reduxMatcher = reduxPattern.matcher(parsedCommand = reduxMatcher.replaceFirst(String.valueOf(commandResultStack.peek())));
                        } else if (command.equalsIgnoreCase("POP")) {
                            reduxMatcher = reduxPattern.matcher(parsedCommand = reduxMatcher.replaceFirst(String.valueOf(commandResultStack.pop())));
                        }
                    } else if (reduxVariables.containsKey(command)) {
                        reduxMatcher = reduxPattern.matcher(parsedCommand = reduxMatcher.replaceFirst(reduxVariables.get(command)));
                    }
                }

                this.successCount = icommandmanager.executeCommand(this, parsedCommand);
                commandResultStack.push(successCount);
                ReduxCommandBlockTileEntity.this.getWorld().setBlockState(blockPos, defaultState.withProperty(ReduxBlock.SUCCESS_COUNT_META, successCount));
                ReduxCommandBlockTileEntity.this.lastSuccessCount = this.successCount;
            }
        }
    }
}
