package mods.quiddity.redux.json;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.*;
import mods.quiddity.redux.Redux;
import mods.quiddity.redux.json.model.Block;
import mods.quiddity.redux.json.model.Config;
import mods.quiddity.redux.json.model.Pack;
import scala.util.parsing.json.JSON;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class handles interaction with all Redux JSON files.
 *
 * @author winsock on 1/23/15.
 */
public class JSONSingleton {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Pack.class, new BlockDeserializer()).create();
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
            Redux.instance.getLogger().fatal("Redux: Error copying default Redux configuration!\nRedux will now cause a crash.", true);
            throw new AssertionError();
        }
    }

    public Config loadConfig() throws JSONLoadException{
        return (Config) loadJSON(configJSON, Config.class);
    }

    public Object loadJSON(File file, Class<?> clazz) throws JSONLoadException {
        try {
            String json = Files.toString(file, charset);
            return gson.fromJson(json, clazz);
        } catch (IOException e) {
            throw new JSONLoadException(e, "Error loading the JSON file: %s", file.getAbsolutePath());
        }
    }

    public Object loadJSON(Reader input, Class<?> clazz) {
        return gson.fromJson(input, clazz);
    }

    public static final class JSONLoadException extends Exception {
        public JSONLoadException (Throwable cause, String format, Object... args) {
            super(String.format(format, args), cause);
        }
    }


    private static class BlockDeserializer implements JsonDeserializer<Pack> {
        private static final Gson normalGson = new Gson();
        @Override
        public Pack deserialize(JsonElement json, Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
            Pack pack = normalGson.fromJson(json, typeOfT);
            final File packSource = Redux.instance.getReduxConfiguration().getSourceForPack(pack);
            JsonElement blockList = json.getAsJsonObject().get("block_list");
            if (blockList.isJsonArray()) {
                JsonArray blockFiles = blockList.getAsJsonArray();
                final List<Block> blocks = new ArrayList<Block>();
                blockFiles.forEach(new Consumer<JsonElement>() {
                    @Override
                    public void accept(JsonElement jsonElement) {
                        if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString()) {
                            String filename = jsonElement.getAsJsonPrimitive().getAsString();
                            Block block = null;
                            if (packSource.isFile() && packSource.getName().endsWith(".zip")) {
                                try {
                                    ZipFile zipFile = new ZipFile(packSource);
                                    ZipEntry blockJson = zipFile.getEntry(filename);
                                    if (blockJson == null) {
                                        throw new JsonParseException("The requested block json file does not exist in the pack!");
                                    } else {
                                        block = normalGson.fromJson(new InputStreamReader(zipFile.getInputStream(blockJson)), Block.class);
                                    }
                                } catch (IOException e) {
                                    throw new JsonParseException(e);
                                }
                            } else if (packSource.getParentFile().isDirectory()) {
                                try {
                                    String json = Files.toString(new File(packSource.getParentFile(), filename), charset);
                                    block = normalGson.fromJson(json, Block.class);
                                } catch (Exception e) {
                                    throw new JsonParseException(e);
                                }
                            }
                            if (block != null) {
                                blocks.add(block);
                            } else {
                                throw new JsonParseException("Unable to load the block json file!");
                            }
                        } else if (jsonElement.isJsonObject()) {
                            Block block = context.deserialize(jsonElement, Block.class);
                            blocks.add(block);
                        }
                    }
                });
                pack.setBlockList(blocks);
            }
            return pack;
        }
    }
}