package mods.quiddity.redux;

import mods.quiddity.redux.json.JSONSingleton;
import mods.quiddity.redux.json.model.Config;
import mods.quiddity.redux.loader.ReduxPackLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * This is the main Redux mod file.
 *
 * @author winsock on 2/3/15.
 */
@Mod(modid = Redux.MODID, version = Redux.VERSION)
public class Redux {

    public static final String MODID = "redux";
    public static final String VERSION = "$DEV";
    public static final String GROUP = "mods/quiddity";
    public static File reduxFolder;

    @Mod.Instance(MODID)
    public static Redux instance = null;

    private Config reduxConfiguration = null;

    public Redux() {
        instance = this;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Redux.reduxFolder = new File(event.getModConfigurationDirectory(), MODID);
        if (!reduxFolder.exists()) {
            // noinspection ResultOfMethodCallIgnored
            reduxFolder.mkdirs();
        }
        reduxConfiguration = JSONSingleton.getInstance().loadConfig();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        // Copy over default Redux Pack
        File defaultPack = new File(reduxFolder, "default_pack.zip");
        if (!defaultPack.exists()) {
            try {
                copyResource(GROUP + "/redux/json/model/default_pack.zip", defaultPack);
            } catch (IOException e) {
                FMLCommonHandler.instance().raiseException(e, "Redux: Error copying default Redux pack!", true);
            }
        }

        // Load the packs
        new ReduxPackLoader(reduxConfiguration).loadPacks();
    }

    public Config getReduxConfiguration() {
        return reduxConfiguration;
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