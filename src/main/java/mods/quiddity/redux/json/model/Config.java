package mods.quiddity.redux.json.model;

import com.google.common.collect.ImmutableList;
import mods.quiddity.redux.Redux;
import mods.quiddity.redux.json.JSONSingleton;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * JSON Object model to handle generic Redux configurations and enabled Redux packs.
 *
 * @author winsock on 2/3/15.
 */
@SuppressWarnings("all")
public class Config {
    private List<Flags<String, ?>> features;
    private List<String> packs;

    private transient List<String> humanPackNames;
    private transient Map<Pack, File> packSourceFileMap;
    private transient Map<String, Pack> idToPack;

    public List<Pack> getPacks() {
        List<Pack> parsedPacks = new ArrayList<Pack>();
        humanPackNames = new ArrayList<String>();
        packSourceFileMap = new HashMap<Pack, File>();
        idToPack = new HashMap<String, Pack>();
        for (String pack : packs) {
            File file = null;
            Pack p = null;
            if (new File(Redux.reduxFolder, pack + File.separator + "config.json").exists()) {
                file = new File(Redux.reduxFolder, pack + File.separator + "config.json");
                p = (Pack) JSONSingleton.getInstance().loadJSON(file, Pack.class);
                if (p == null) {
                    LogManager.getLogger().warn("Enabled Redux pack config file not found. % s does not exist.", Redux.reduxFolder.getAbsolutePath() + pack + File.separator + "config.json");
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
                } catch (IOException e) {
                    LogManager.getLogger().warn("Enabled Redux pack inconsistency. %s is inconsistent.", pack + ".zip");
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
        return ImmutableList.copyOf(parsedPacks);
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
        return ImmutableList.copyOf(features);
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
