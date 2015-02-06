package mods.quiddity.redux;

import mods.quiddity.redux.json.model.Block;
import mods.quiddity.redux.json.model.Pack;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Stub class that represents all Redux pack blocks.
 *
 * @author winsock on 2/3/15.
 */
public class ReduxBlock extends net.minecraft.block.Block implements ITileEntityProvider {

    @SuppressWarnings("all")
    private final Pack pack;
    private final Block reduxBlock;
    public static final PropertyInteger SUCCESS_COUNT_META = PropertyInteger.create("lastSuccessCount", 0, 15);


    public ReduxBlock(Pack parentPack, Block reduxBlock) {
        super(reduxBlock.getMaterial());
        setUnlocalizedName(reduxBlock.getName());
        setCreativeTab(reduxBlock.getCreativeTab());
        this.pack = parentPack;
        this.reduxBlock = reduxBlock;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return reduxBlock.tickable() || !reduxBlock.getScript().isEmpty();
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return createNewTileEntity(world, 0);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        if (reduxBlock.tickable())
            return new ReduxCommandBlockTickableTileEntity(reduxBlock);
        if (!reduxBlock.getScript().isEmpty())
            return new ReduxCommandBlockTileEntity(reduxBlock);
        return null;
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return hasTileEntity(null);
    }

    @Override
    public int getComparatorInputOverride(World worldIn, BlockPos pos) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity instanceof ReduxCommandBlockTileEntity ? ((ReduxCommandBlockTileEntity)tileentity).getLastSuccessCount() : 0;
    }

    public int getMetaFromState(IBlockState state) {
        if (hasTileEntity(state) && !state.getPropertyNames().isEmpty()) {
            return (Integer)state.getValue(SUCCESS_COUNT_META);
        }
        return 0;
    }
}