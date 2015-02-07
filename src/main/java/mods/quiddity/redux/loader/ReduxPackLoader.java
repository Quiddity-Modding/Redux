package mods.quiddity.redux.loader;

import mods.quiddity.redux.Redux;
import mods.quiddity.redux.ReduxBlock;
import mods.quiddity.redux.json.model.Block;
import mods.quiddity.redux.json.model.Config;
import mods.quiddity.redux.json.model.Pack;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the Redux packs
 * Configures all of the items, blocks, and recipes with Minecraft/FML.
 *
 * @author winsock on 2/3/15.
 */
public class ReduxPackLoader {

    private final Config configuration;

    public ReduxPackLoader(Config mainConfig) {
        this.configuration = mainConfig;
    }

    @SuppressWarnings("unchecked")
    public void loadPacks() {
        List<ModContainer> children = new ArrayList<ModContainer>();
        for (Pack p : configuration.getPacks()) {
            ModContainer packContainer = new ReduxPackModContainer(p, Redux.instance);
            FMLCommonHandler.instance().addModToResourcePack(packContainer);
            children.add(packContainer);

            for (Block b : p.getBlocks()) {
                ReduxBlock.blockThreadLocal.set(b);
                ReduxBlock mcBlock = new ReduxBlock(p, b);
                ReduxBlock.blockThreadLocal.remove();
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

                    if (mcBlock.hasTileEntity(null) && mcBlock.getTileEntityClass() != null) {
                        TileEntity.addMapping(mcBlock.getTileEntityClass(), b.getId());
                    }
                } catch (Exception e) {
                    Redux.instance.getLogger().fatal("Error accessing FML GameData.\nRedux will not function properly!\nDid FML Update?", e);
                }
            }
            /*
             * TODO: Load the other pack components
             */
        }
        FMLCommonHandler.instance().findContainerFor(Redux.instance).getMetadata().childMods = children;
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            FMLClientHandler.instance().addSpecialModEntries((ArrayList<ModContainer>) children);
        }
    }
}
