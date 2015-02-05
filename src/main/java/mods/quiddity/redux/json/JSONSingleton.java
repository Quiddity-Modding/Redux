package mods.quiddity.redux.json;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mods.quiddity.redux.Redux;
import mods.quiddity.redux.json.model.Config;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * This class handles interaction with all Redux JSON files.
 *
 * @author winsock on 1/23/15.
 */
public class JSONSingleton {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Charset charset = Charsets.UTF_8;
    private static final File configJSON = new File(Redux.reduxFolder, File.separator + "config.json");

    private static JSONSingleton ourInstance = new JSONSingleton();

    public static JSONSingleton getInstance() {
        return ourInstance;
    }

    private JSONSingleton() {
        try {
            if (!configJSON.exists()) {
                Redux.copyResource(Redux.GROUP + "/redux/json/model/config.json", configJSON);
            }
        } catch (IOException e) {
            FMLCommonHandler.instance().raiseException(e, "Redux: Error copying default Redux configuration!", true);
        }
    }

    public Config loadConfig() {
        return (Config) loadJSON(configJSON, Config.class);
    }

    public Object loadJSON(File file, Class<?> clazz) {
        try {
            String json = Files.toString(file, charset);
            return gson.fromJson(json, clazz);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object loadJSON(Reader input, Class<?> clazz) {
        return gson.fromJson(input, clazz);
    }
}