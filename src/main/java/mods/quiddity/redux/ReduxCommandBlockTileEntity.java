package mods.quiddity.redux;

import mods.quiddity.redux.json.model.Block;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The tile entity for Redux Pack blocks that have custom command scripts.
 *
 * @author winsock on 2/5/15.
 */
public class ReduxCommandBlockTileEntity extends TileEntity {

    protected volatile Block reduxBlock = null;
    protected int lastSuccessCount = 0;
    protected CommandResultStats.Type lastResultType = CommandResultStats.Type.SUCCESS_COUNT;
    protected int lastResultAmount = 0;

    @SuppressWarnings("all")
    protected final List<ReduxBlockEventReceiver> eventReceivers = new ArrayList<ReduxBlockEventReceiver>();
    protected final Set<ReduxBlockEventReceiver> tickEventReceivers = new HashSet<ReduxBlockEventReceiver>();

    public ReduxCommandBlockTileEntity() {}

    public synchronized void setupTileEntity(Block reduxBlock) {
        this.reduxBlock = reduxBlock;

        for (Trigger trigger : reduxBlock.getScript()) {
            try {
                eventReceivers.add(new ReduxBlockEventReceiver(trigger));
            } catch (ReflectiveOperationException e) {
                FMLLog.severe("Error accessing FML EventBus.\nRedux will not function properly!\nDid FML Update?");
            }
        }
    }

    public int getLastSuccessCount() {
        return lastSuccessCount;
    }

    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("lastSuccessCount", lastSuccessCount);
        compound.setInteger(lastResultType.getTypeName(), lastResultAmount);
    }

    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.lastSuccessCount = compound.getInteger("lastSuccessCount");

        if (reduxBlock == null && ! (worldObj == null || worldObj.isRemote)) {
            setupTileEntity(((ReduxBlock) this.getWorld().getBlockState(this.pos).getBlock()).getReduxBlock());
        }
    }

    public Packet getDescriptionPacket() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.pos, 2, nbttagcompound);
    }

    public void addTickEventReceiver(ReduxBlockEventReceiver receiver) {
        tickEventReceivers.add(receiver);
    }

    protected class ReduxBlockEventReceiver implements ICommandSender {

        private final Trigger triggerScript;
        private int successCount;

        public ReduxBlockEventReceiver(Trigger triggerScript) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            this.triggerScript = triggerScript;

            if (triggerScript.getTriggerEvent() != Event.class) {
                Class<?> eventBusClass = EventBus.class;
                Method privateEventRegister = eventBusClass.getDeclaredMethod("register", Class.class, Object.class, Method.class, ModContainer.class);
                privateEventRegister.setAccessible(true);

                Method eventReceiver = this.getClass().getDeclaredMethod("receiveEvent", Event.class);
                privateEventRegister.invoke(MinecraftForge.EVENT_BUS, triggerScript.getTriggerEvent(), this, eventReceiver, FMLCommonHandler.instance().findContainerFor(Redux.instance));
            } else if (ReduxCommandBlockTileEntity.this instanceof  ReduxCommandBlockTickableTileEntity) {
                ReduxCommandBlockTileEntity.this.addTickEventReceiver(this);
            }
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

        @SubscribeEvent
        @SuppressWarnings("all")
        public void receiveEvent(Event ignored) {
            ICommandManager icommandmanager = FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
            synchronized (ReduxCommandBlockTileEntity.this) {
                BlockPos blockPos = ReduxCommandBlockTileEntity.this.pos;
                IBlockState defaultState = ReduxCommandBlockTileEntity.this.getWorld().getBlockState(blockPos).getBlock().getDefaultState();
                for (String s : triggerScript.getCommands()) {
                    this.successCount = icommandmanager.executeCommand(this, s);
                    ReduxCommandBlockTileEntity.this.getWorld().setBlockState(blockPos, defaultState.withProperty(ReduxBlock.SUCCESS_COUNT_META, successCount));
                    ReduxCommandBlockTileEntity.this.lastSuccessCount = this.successCount;
                }
            }
        }
    }
}
