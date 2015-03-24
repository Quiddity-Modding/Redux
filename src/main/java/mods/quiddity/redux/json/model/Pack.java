package mods.quiddity.redux.json.model;

import com.google.common.collect.ImmutableList;
import mods.quiddity.redux.JavaScript.ReduxJavascriptEngine;
import mods.quiddity.redux.Redux;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON Object model to represent Redux packs.
 *
 * @author winsock on 2/3/15.
 */
@SuppressWarnings("all")
public class Pack {
    /**
     * <h1>Required</h1>
     * The mod id of the pack. Must not conflict with any other {@link net.minecraftforge.fml.common.FMLModContainer}.
     * <em>Should contain no spaces or special characters.</em>
     * <em>ASCII</em>
     */
    @Nonnull
    protected String id;

    /**
     * <h1>Required</h1>
     * The human readable name of the mod.
     * Will be displayed as a child-mod of Redux in the mods list
     */
    @Nonnull
    protected String name;

    /**
     * The author of the Redux pack.
     * <em>Curently doesn't display anywhere</em>
     */
    @Nullable
    protected String author;

    /**
     * The author's description of the Redux pack.
     * <em>Curently doesn't display anywhere</em>
     */
    @Nullable
    protected String description;

    /**
     * A list of blocks to load.
     * Not <i>required</i> however the pack will not have any new blocks without them
     * {@link mods.quiddity.redux.json.model.Block}
     */
    @Nullable
    protected transient List<Block> block_list;

    /**
     * A list of items to load.
     * Not <i>required</i> however the pack will not have any new items without them
     * {@link mods.quiddity.redux.json.model.Item}
     *
     * <em>Unimplemented</em>
     */
    @Nullable
    protected List<Item> item_list;

    private transient Map<String, Block> idMap = null;
    private transient boolean hasAddedBlocks = false;
    private transient ReduxJavascriptEngine jsEngine;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public List<Block> getBlocks() {
        return block_list == null ? null : block_list;
    }

    public void setBlockList(List<Block> blocks) {
        if (!hasAddedBlocks) {
            block_list = ImmutableList.copyOf(blocks);
            hasAddedBlocks = true;
        }
    }

    public Block getBlockFromId(String id) {
        if (getBlocks() == null || id == null)
            return null;
        if (idMap == null) {
            idMap = new HashMap<String, Block>();
            for (Block b : getBlocks()) {
                idMap.put(b.getId(), b);
            }
        }
        return idMap.get(id);
    }

    public List<Item> getItems() {
        return item_list == null ? null : ImmutableList.copyOf(item_list);
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public ReduxJavascriptEngine getJsEngine() {
        if (jsEngine == null) {
            jsEngine = new ReduxJavascriptEngine(this);
            jsEngine.init();
            File packFile = Redux.instance.getReduxConfiguration().getSourceForPack(this);
            if (packFile.getParentFile().isDirectory()) {
                File scripts = new File(packFile.getParentFile(), "scripts");
                for (File script : FileUtils.listFiles(scripts, new String[]{"js"}, true)) {
                    try {
                        jsEngine.getEngine().loadScript(FileUtils.readFileToString(script));
                    } catch (Exception e) {
                        Redux.instance.getLogger().warn("Redux pack inconsistency. The script file: %s has errors.", scripts.getName());
                    }
                }
            } else {
                try {
                    ZipFile packZip = new ZipFile(packFile.getParentFile());
                    Enumeration<ZipArchiveEntry> entries = packZip.getEntries();
                    while (entries.hasMoreElements()) {
                        ZipArchiveEntry entry = entries.nextElement();
                        if (entry.getName().endsWith(".js")) {
                            InputStream scriptInputStream = packZip.getInputStream(entry);
                            jsEngine.getEngine().loadScript(IOUtils.toString(scriptInputStream, Charset.defaultCharset()));
                        }
                    }
                } catch (IOException e) {
                    Redux.instance.getLogger().warn("Redux pack inconsistency. The pack file %s has vanished!.", packFile.getParentFile().getName());
                } catch (ScriptException e) {
                    Redux.instance.getLogger().warn("Redux pack inconsistency. A script file in pack: %s has errors.", packFile.getParentFile().getName());
                }
            }
        }
        return jsEngine;
    }

    @Override
    public String toString() {
        return getName();
    }
}
