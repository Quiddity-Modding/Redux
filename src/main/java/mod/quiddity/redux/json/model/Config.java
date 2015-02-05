package mod.quiddity.redux.json.model;

import mod.quiddity.redux.Redux;
import mod.quiddity.redux.json.JSONSingleton;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * JSON Object model to handle generic Redux configurations and enabled Redux packs.
 *
 * @author winsock on 2/3/15.
 */
public class Config {
    @SuppressWarnings("all")
    private List<String> packs;

    private transient List<String> humanPackNames;
    private transient Map<Pack, File> packSourceFileMap;

    public List<Pack> getPacks() {
        List<Pack> parsedPacks = new ArrayList<Pack>();
        humanPackNames = new ArrayList<String>();
        packSourceFileMap = new HashMap<Pack, File>();
        for (String pack : packs) {
            File file = null;
            Pack p = null;
            if (new File(Redux.reduxFolder, pack + File.separator + "config.json").exists()) {
                file = new File(Redux.reduxFolder, pack + File.separator + "config.json");
                p = (Pack) JSONSingleton.getInstance().loadJSON(file, Pack.class);
                if (p == null) {
                    FMLLog.warning("Enabled Redux pack config file not found. %s does not exist.", Redux.reduxFolder.getAbsolutePath() + pack + File.separator + "config.json");
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
                    FMLLog.warning("Enabled Redux pack inconsistency. %s is inconsistent.", pack + ".zip");
                }
            }
            if (p != null) {
                parsedPacks.add(p);
                humanPackNames.add(p.getName());
                // File cannot be null if p isn't null
                packSourceFileMap.put(p, file);
            }
        }
        return parsedPacks;
    }

    public List<String> getPackNames() {
        if (humanPackNames == null) {
            getPacks();
        }
        return humanPackNames;
    }

    public File getSourceForPack(Pack p) {
        if (packSourceFileMap == null) {
            getPacks();
        }
        return packSourceFileMap.get(p);
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
