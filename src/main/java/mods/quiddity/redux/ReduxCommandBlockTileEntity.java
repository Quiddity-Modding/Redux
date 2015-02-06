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
import java.util.List;

/**
 * The tile entity for Redux Pack blocks that have custom command scripts.
 *
 * @author winsock on 2/5/15.
 */
public class ReduxCommandBlockTileEntity extends TileEntity {

    protected final Block reduxBlock;
    protected int lastSuccessCount = 0;

    @SuppressWarnings("all")
    private final List<ReduxBlockEventReceiver> eventReceivers = new ArrayList<ReduxBlockEventReceiver>();

    public ReduxCommandBlockTileEntity(Block block) {
        reduxBlock = block;

        for (Trigger trigger : block.getScript()) {
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
    }

    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.lastSuccessCount = compound.getInteger("lastSuccessCount");
    }

    public Packet getDescriptionPacket() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.pos, 2, nbttagcompound);
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
                ReduxCommandBlockTickableTileEntity tickableTileEntity = (ReduxCommandBlockTickableTileEntity) ReduxCommandBlockTileEntity.this;
                tickableTileEntity.addTickEventReceiver(this);
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
            BlockPos blockPos = ReduxCommandBlockTileEntity.this.pos;
            IBlockState defaultState = ReduxCommandBlockTileEntity.this.getWorld().getBlockState(blockPos).getBlock().getDefaultState();
            ReduxCommandBlockTileEntity.this.getWorld().setBlockState(blockPos, defaultState.withProperty(ReduxBlock.COMMAND_RESULTS, type).withProperty(ReduxBlock.COMMAND_RESULTS_VALUE, amount));
        }

        @SubscribeEvent
        @SuppressWarnings("all")
        public void receiveEvent(Event ignored) {
            ICommandManager icommandmanager = FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
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
