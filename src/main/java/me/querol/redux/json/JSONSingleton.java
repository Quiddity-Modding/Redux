package me.querol.redux.json;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.querol.redux.json.model.Blocks;
import me.querol.redux.json.model.Recipes;
import net.minecraftforge.fml.relauncher.FMLInjectionData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

/**
 * Created by winsock on 1/23/15.
 */
public class JSONSingleton {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Charset charset = Charsets.UTF_8;
    private static final File defaultSettingsFolder = new File(/* The minecraft dir */(File) FMLInjectionData.data()[6], "config" + File.separator + "redux");

    private static JSONSingleton ourInstance = new JSONSingleton();

    public static JSONSingleton getInstance() {
        return ourInstance;
    }

    private JSONSingleton() {
        if (!defaultSettingsFolder.exists()) {
            defaultSettingsFolder.mkdirs();
        }

        File blocksJson = new File(defaultSettingsFolder, File.separator + "blocks.json");
        File recipesJson = new File(defaultSettingsFolder, File.separator + "recipes.json");
        try {
            if (!blocksJson.exists()) {
                copyResource("me/querol/redux/json/model/blocks.json", blocksJson);
            }
            if (!recipesJson.exists()) {
                copyResource("me/querol/redux/json/model/recipes.json", recipesJson);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadJSON() {
        Blocks blocks = (Blocks) loadJSON(new File(defaultSettingsFolder, File.separator + "blocks.json"), Blocks.class);
        Recipes recipes = (Recipes) loadJSON(new File(defaultSettingsFolder, File.separator + "recipes.json"), Recipes.class);

        /*
        * FIXME This is just and example
        */
        System.out.println("test");
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

    private static void copyResource(String sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        ReadableByteChannel source = null;
        FileChannel destination = null;
        FileOutputStream fileOutput = null;
        InputStream resource = null;

        try {
            resource = JSONSingleton.class.getClassLoader().getResourceAsStream(sourceFile);
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