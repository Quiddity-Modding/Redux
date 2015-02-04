package me.querol.redux;

import me.querol.redux.json.JSONSingleton;
import me.querol.redux.json.model.Config;
import me.querol.redux.loader.ReduxBlockLoader;
import me.querol.redux.loader.ReduxResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.List;


@Mod(modid = Redux.MODID, version = Redux.VERSION)
public class Redux
{
    public static final String MODID = "redux";
    public static final String VERSION = "$DEV";
    public static File reduxFolder;
    public static ReduxResourcePack resourcePack;

    @SuppressWarnings("all")
    private Config reduxConfiguration = null;
    @SuppressWarnings("all")
    private ReduxBlockLoader blockLoader = null;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Redux.reduxFolder = new File(event.getModConfigurationDirectory(), MODID);
        if (!reduxFolder.exists()) {
            // noinspection ResultOfMethodCallIgnored
            reduxFolder.mkdirs();
        }
        Redux.resourcePack = new ReduxResourcePack(reduxFolder);
        reduxConfiguration = JSONSingleton.getInstance().loadConfig();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        // Copy over default Redux Pack
        File defaultPack = new File(reduxFolder, "default_pack.zip");
        if (!defaultPack.exists()) {
            try {
                copyResource("me/querol/redux/json/model/default.zip", defaultPack);
            } catch (IOException e) {
                FMLCommonHandler.instance().raiseException(e, "Redux: Error copying default Redux pack!", true);
            }
        }

        if (event.getSide() == Side.CLIENT) {
            Class<FMLClientHandler> fmlClientClass = FMLClientHandler.class;
            try {
                Field resourcePackListField = fmlClientClass.getDeclaredField("resourcePackList");
                resourcePackListField.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<IResourcePack> resourcePackList = (List<IResourcePack>) resourcePackListField.get(FMLClientHandler.instance());
                resourcePackList.add(resourcePack);
            } catch (ReflectiveOperationException e) {
                FMLLog.severe("Error accessing resource pack list.\nDid FML Update?");
            }
        }

        blockLoader = new ReduxBlockLoader(reduxConfiguration);
        blockLoader.loadBlocks();
    }

    public static void copyResource(String sourceFile, File destFile) throws IOException {

        if (!destFile.exists()) {
            // noinspection ResultOfMethodCallIgnored
            destFile.createNewFile();
        }

        ReadableByteChannel source = null;
        FileChannel destination = null;
        FileOutputStream fileOutput = null;
        InputStream resource = null;

        try {
            resource = Redux.class.getClassLoader().getResourceAsStream(sourceFile);
            source = Channels.newChannel(resource);
            fileOutput = new FileOutputStream(destFile);
            destination = fileOutput.getChannel();
            destination.transferFrom(source, 0, resource.available());
        } finally {
            if (source != null)
                source.close();
            if (destination != null)
                destination.close();
            if (fileOutput != null)
                fileOutput.close();
            if (resource != null)
                resource.close();
        }
    }
}