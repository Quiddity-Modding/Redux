package me.querol.redux.loader;

import me.querol.redux.Redux;
import me.querol.redux.ReduxBlock;
import me.querol.redux.json.model.Block;
import me.querol.redux.json.model.Config;
import me.querol.redux.json.model.Pack;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by winsock on 2/3/15.
 */
public class ReduxBlockLoader {

    private final Config configuration;
    private final Set<ReduxBlock> loadedBlocks = new HashSet<ReduxBlock>();

    public ReduxBlockLoader(Config mainConfig) {
        this.configuration = mainConfig;
    }

    public void loadBlocks() {
        for (Pack p : configuration.getPacks()) {
            Redux.resourcePack.addDomain(p.getId());
            for (Block b : p.getBlocks()) {
                ReduxBlock mcBlock = new ReduxBlock(p, b);
                Item blockItem = new ItemBlock(mcBlock);

                if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
                    FMLClientHandler.instance().getClient().getRenderItem().getItemModelMesher().register(blockItem, 0,
                            new ModelResourceLocation(p.getId() + ":" + b.getId(), "inventory"));
                }

                try {
                    Class<GameData> gameDataClass = GameData.class;
                    Method mainDataGetMethod = gameDataClass.getDeclaredMethod("getMain");
                    mainDataGetMethod.setAccessible(true);
                    Method registerBlockMethod = gameDataClass.getDeclaredMethod("registerBlock", net.minecraft.block.Block.class, String.class, int.class);
                    registerBlockMethod.setAccessible(true);
                    Method registerItemMethod = gameDataClass.getDeclaredMethod("registerItem", net.minecraft.item.Item.class, String.class, int.class);
                    registerItemMethod.setAccessible(true);
                    GameData gameData = (GameData) mainDataGetMethod.invoke(null);
                    registerBlockMethod.invoke(gameData, mcBlock, p.getId() + ":" + b.getId(), -1);
                    registerItemMethod.invoke(gameData, blockItem, p.getId() + ":" + b.getId(), -1);
                    GameData.getBlockItemMap().put(mcBlock, blockItem);
                } catch (ReflectiveOperationException e) {
                    FMLLog.severe("Error accessing FML GameData.\nRedux will not function properly!\nDid FML Update?");
                }
                loadedBlocks.add(mcBlock);
            }
            /*
             * TODO: Load the other pack components
             */
        }
    }
}
