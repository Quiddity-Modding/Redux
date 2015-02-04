package me.querol.redux.json.model;

import me.querol.redux.Redux;
import me.querol.redux.json.JSONSingleton;
import net.minecraftforge.fml.common.FMLLog;

import java.io.File;
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
            Pack p = (Pack) JSONSingleton.getInstance().loadJSON(new File(Redux.reduxFolder, pack + File.separator + "config.json"), Pack.class);
            if (p == null) {
                FMLLog.warning("Enabled Redux pack config file not found. %s does not exist.", Redux.reduxFolder.getAbsolutePath() + pack + File.separator + "config.json");
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
