package mods.quiddity.redux.json.model;

import com.google.common.collect.ImmutableList;
import mods.quiddity.redux.Redux;
import mods.quiddity.redux.json.JSONSingleton;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.logging.log4j.message.FormattedMessage;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON Object model to handle generic Redux configurations and enabled Redux packs.
 *
 * @author winsock on 2/3/15.
 */
@SuppressWarnings("all")
public class Config {
    /**
     * A list of feature flags. No flags are implemented yet
     * {@link mods.quiddity.redux.json.model.Flags}
     */
    @Nullable
    protected List<Flags<String, ?>> features;

    /**
     * A list of packs to load. Must be the same name as the pack folder/zip and id
     * Not <i>required</i> however this mod does nothing without them
     * {@link mods.quiddity.redux.json.model.Pack}
     */
    @Nullable
    protected List<String> packs;

    private transient List<Pack> parsedPacks;
    private transient List<String> humanPackNames;
    private transient Map<Pack, File> packSourceFileMap;
    private transient Map<String, Pack> idToPack;

    public List<Pack> getPacks() {
        if (parsedPacks != null)
            return parsedPacks;
        parsedPacks = new ArrayList<Pack>();
        humanPackNames = new ArrayList<String>();
        packSourceFileMap = new HashMap<Pack, File>();
        idToPack = new HashMap<String, Pack>();
        for (String pack : packs) {
            File file = null;
            Pack p = null;
            if (new File(Redux.reduxFolder, pack + File.separator + "config.json").exists()) {
                file = new File(Redux.reduxFolder, pack + File.separator + "config.json");
                try {
                    p = (Pack) JSONSingleton.getInstance().loadJSON(file, Pack.class);
                    if (p == null) {
                        Redux.instance.getLogger().warn("Enabled Redux pack config file not found. % s does not exist.", Redux.reduxFolder.getAbsolutePath() + pack + File.separator + "config.json");
                    }
                } catch (JSONSingleton.JSONLoadException e) {
                    Redux.instance.getLogger().warn("Enabled Redux pack inconsistency. %s is inconsistent. Check the configuration.", pack + ".zip");
                    Redux.instance.getLogger().warn(new FormattedMessage("Redux pack %s will not be loaded.", pack, e));
                }
            } else if (new File(Redux.reduxFolder, pack + ".zip").exists()) {
                file = new File(Redux.reduxFolder, pack + ".zip");
                try {
                    ZipFile packZip = new ZipFile(file);
                    ZipArchiveEntry packConfig = packZip.getEntry("config.json");
                    if (packConfig != null) {
                        InputStreamReader packZipReader = new InputStreamReader(packZip.getInputStream(packConfig));
                        p = (Pack) JSONSingleton.getInstance().loadJSON(packZipReader, Pack.class);
                    }
                    packZip.close();
                } catch (Exception e) {
                    Redux.instance.getLogger().warn("Enabled Redux pack inconsistency. %s is inconsistent. Check the configuration.", pack + ".zip");
                    Redux.instance.getLogger().warn(new FormattedMessage("Redux pack %s will not be loaded.", pack, e));
                }
            }
            if (p != null) {
                parsedPacks.add(p);
                humanPackNames.add(p.getName());
                // File cannot be null if p isn't null
                packSourceFileMap.put(p, file);
                idToPack.put(p.getId(), p);
            }
        }
        return parsedPacks;
    }

    public List<String> getPackNames() {
        if (humanPackNames == null) {
            getPacks();
        }
        return ImmutableList.copyOf(humanPackNames);
    }

    public File getSourceForPack(Pack p) {
        if (packSourceFileMap == null) {
            getPacks();
        }
        return packSourceFileMap.get(p);
    }

    public Pack getPackFromId(String id) {
        return idToPack.get(id);
    }

    public List<Flags<String, ?>> getFeatures() {
        if (features == null)
            return null;
        return ImmutableList.copyOf(features);
    }

    public Flags<String, ?> getFlagForName(String key) {
        return this.getFlagForName(key, null);
    }

    public Flags<String, ?> getFlagForName(String key, Flags<String, ?> defaultValue) {
        if (features == null)
            return null;
        for (Flags<String, ?> flag : ImmutableList.copyOf(features)) {
            if (flag.getKey().equalsIgnoreCase(key))
                return flag;
        }
        return defaultValue;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("Enabled Packs: [");
        for (String s : getPackNames()) {
            string.append(s);
            string.append(',');
        }
        string.replace(string.lastIndexOf(","), string.lastIndexOf(","), "]");
        return string.toString();
    }
}
