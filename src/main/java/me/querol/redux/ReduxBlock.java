package me.querol.redux;

import me.querol.redux.json.model.Block;
import me.querol.redux.json.model.Pack;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

/**
 * Stub class that represents all Redux pack blocks.
 *
 * @author winsock on 2/3/15.
 */
public class ReduxBlock extends net.minecraft.block.Block {
    @SuppressWarnings("all") // TODO: Remove after finishing ReduxBlock
    public ReduxBlock(Pack parentPack, Block reduxBlock) {
        super(Material.rock);
        setUnlocalizedName(reduxBlock.getName());
        setCreativeTab(CreativeTabs.tabMisc);
    }
}