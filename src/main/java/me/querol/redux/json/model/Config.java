package me.querol.redux.json.model;

import me.querol.redux.Redux;
import me.querol.redux.json.JSONSingleton;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by winsock on 2/3/15.
 */
public class Config {
    private List<String> packs;

    public List<Pack> getPacks() {
        List<Pack> parsedPacks = new ArrayList<Pack>();
        for (String pack : packs) {
            File file = null;
            Pack p = null;
            if (new File(Redux.reduxFolder, pack + File.separator + "config.json").exists()) {
                p = (Pack) JSONSingleton.getInstance().loadJSON(new File(Redux.reduxFolder, pack + File.separator + "config.json"), Pack.class);
                if (p == null) {
                    FMLLog.warning("Enabled Redux pack config file not found. %s does not exist.", Redux.reduxFolder.getAbsolutePath() + pack + File.separator + "config.json");
                }
            } else if (new File(Redux.reduxFolder, pack + ".zip").exists()) {
                try {
                    ZipFile packZip = new ZipFile(new File(Redux.reduxFolder, pack + ".zip"));
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

            parsedPacks.add(p);
        }
        return parsedPacks;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("Enabled Packs: [");
        for (String s : packs) {
            string.append(s + ",");
        }
        string.replace(string.lastIndexOf(","), string.lastIndexOf(","), "]");
        return string.toString();
    }
}
