package mods.quiddity.redux.JavaScript;

import mods.quiddity.redux.json.model.Pack;
import net.minecraft.command.CommandResultStats;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.script.ScriptEngineManager;

public class ReduxJavascriptEngine {
    public static final ScriptEngineManager engineManager = new ScriptEngineManager();
    private JavascriptEngine engine;
    private final Pack packRefrence;

    public ReduxJavascriptEngine(Pack pack) {
        if (engineManager.getEngineByName("nashorn") != null) {
            engine = new NashornEngine();
        } else if (engineManager.getEngineByName("rhino") != null) {
            engine = new RhinoEngine();
        } else {
            throw new AssertionError("Your Java Runtime Environment does not support JSR-223");
        }

        engine.addJavaObject("ReduxAPI", new ReduxAPI());

        this.packRefrence = pack;
    }

    public JavascriptEngine getEngine() {
        return engine;
    }

    public class ReduxAPI implements ICommandSender {
        @SuppressWarnings("unused")
        public int runCommand(String... args) {
            if (FMLCommonHandler.instance().getSide().isServer()) {
                ICommandManager manager = FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
                manager.executeCommand(this, StringUtils.join(args));
            }
            return 0;
        }

        @Override
        public String getName() {
            return packRefrence.getName();
        }

        @Override
        public IChatComponent getDisplayName() {
            return new ChatComponentText(packRefrence.getName());
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
            return null;
        }

        @Override
        public Vec3 getPositionVector() {
            return new Vec3(0, 0, 0);
        }

        @Override
        public World getEntityWorld() {
            return null;
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
        }
    }
}
