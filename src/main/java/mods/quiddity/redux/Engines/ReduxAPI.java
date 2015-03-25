package mods.quiddity.redux.Engines;

import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.lang3.StringUtils;

public class ReduxAPI implements ICommandSender {
    private Engine reduxEngine;
    private Entity lastEntity = null;
    private World lastWorld = null;
    private BlockPos lastBlockPos = null;

    public ReduxAPI(Engine reduxEngine) {
        this.reduxEngine = reduxEngine;
    }

    @SuppressWarnings("unused")
    public int runCommand(String... args) {
        if (FMLCommonHandler.instance().getMinecraftServerInstance() != null &&
                FMLCommonHandler.instance().getMinecraftServerInstance().isCallingFromMinecraftThread()) {
            if (reduxEngine.globalObjectExists("entity") && reduxEngine.getJavaObject("entity", Entity.class) instanceof Entity) {
                lastEntity = (Entity) reduxEngine.getJavaObject("entity", Entity.class);
            } else {
                lastEntity = null;
            }
            if (reduxEngine.globalObjectExists("world") && reduxEngine.getJavaObject("world", World.class) instanceof World) {
                lastWorld = (World) reduxEngine.getJavaObject("world", World.class);
            } else {
                lastWorld = null;
            }
            if (reduxEngine.globalObjectExists("pos") && reduxEngine.getJavaObject("pos", BlockPos.class) instanceof BlockPos) {
                lastBlockPos = (BlockPos) reduxEngine.getJavaObject("pos", BlockPos.class);
            } else {
                lastBlockPos = null;
            }
            ICommandManager manager = FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
            manager.executeCommand(this, StringUtils.join(args));
        }
        return 0;
    }

    @Override
    public String getName() {
        return reduxEngine.getPackReference().getName();
    }

    @Override
    public IChatComponent getDisplayName() {
        return new ChatComponentText(reduxEngine.getPackReference().getName());
    }

    @Override
    public void addChatMessage(IChatComponent message) {
    }

    @Override
    public boolean canUseCommand(int permLevel, String commandName) {
        return permLevel <= 2;
    }

    @Override
    public BlockPos getPosition() {
        return lastBlockPos == null ? lastEntity == null ? new BlockPos(0, 0, 0) : lastEntity.getPosition() :lastBlockPos;
    }

    @Override
    public Vec3 getPositionVector() {
        return lastBlockPos == null ? lastEntity == null ? new Vec3(0, 0, 0) : lastEntity.getPositionVector() : new Vec3(lastBlockPos.getX(), lastBlockPos.getY(), lastBlockPos.getZ());
    }

    @Override
    public World getEntityWorld() {
        return lastWorld == null ? lastEntity == null ? null : lastEntity.worldObj : lastWorld;
    }

    @Override
    public Entity getCommandSenderEntity() {
        return lastEntity;
    }

    @Override
    public boolean sendCommandFeedback() {
        return false;
    }

    @Override
    public void setCommandStat(CommandResultStats.Type type, int amount) {
    }
}
