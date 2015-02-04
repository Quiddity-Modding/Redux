package me.querol.redux.json;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.querol.redux.Redux;
import me.querol.redux.json.model.Config;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by winsock on 1/23/15.
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
                Redux.copyResource("me/querol/redux/json/model/config.json", configJSON);
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
            Object parsedJson = gson.fromJson(json, clazz);
            return parsedJson;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}