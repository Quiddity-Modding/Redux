package mods.quiddity.redux;

import mods.quiddity.redux.json.model.Block;
import mods.quiddity.redux.json.model.Flags;
import mods.quiddity.redux.json.model.Pack;
import mods.quiddity.redux.json.model.Trigger;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stub class that represents all Redux pack blocks.
 *
 * @author winsock on 2/3/15.
 */
public class ReduxBlock extends net.minecraft.block.Block implements ITileEntityProvider {

    private final Pack pack;
    private final Block reduxBlock;
    public static final PropertyInteger SUCCESS_COUNT_META = PropertyInteger.create("lastSuccessCount", 0, 15);
    public static final ThreadLocal<Block> blockThreadLocal = new ThreadLocal<Block>();

    private final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    private Map<String, PropertyInteger> customBlockProperties = new HashMap<String, PropertyInteger>();
    private int lastRedstoneStrongStrength[] = {0, 0, 0, 0, 0, 0}, lastRedstoneWeakStrength = 0;

    public ReduxBlock(Pack parentPack, Block reduxBlock) {
        super(reduxBlock.getMaterial());
        this.pack = parentPack;
        this.reduxBlock = reduxBlock;

        setUnlocalizedName(reduxBlock.getName());
        setCreativeTab(reduxBlock.getCreativeTab());

        IBlockState defaultBlockState = this.blockState.getBaseState().withProperty(SUCCESS_COUNT_META, 0);
        if (reduxBlock.shouldAddFacingProperty())
            defaultBlockState = defaultBlockState.withProperty(FACING, null);
        if (reduxBlock.getCustomProperties() != null) {
            for (Flags<String, Integer> customProperty : reduxBlock.getCustomProperties()) {
                PropertyInteger customIntegerProperty = PropertyInteger.create(customProperty.getKey(), Integer.MIN_VALUE, Integer.MAX_VALUE);
                customBlockProperties.put(customProperty.getKey(), customIntegerProperty);
                defaultBlockState = defaultBlockState.withProperty(customIntegerProperty, customProperty.getValue());
            }
        }
        this.setDefaultState(defaultBlockState);

        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            StateMap.Builder stateMapBuilder = (new StateMap.Builder()).addPropertiesToIgnore(SUCCESS_COUNT_META);
            if (reduxBlock.getIgnoredProperties() != null) {
                for (String s : reduxBlock.getIgnoredProperties()) {
                    PropertyInteger propertyInteger = customBlockProperties.get(s);
                    if (propertyInteger != null) {
                        stateMapBuilder.addPropertiesToIgnore(propertyInteger);
                    }
                }
            }
            FMLClientHandler.instance().getClient().getBlockRendererDispatcher().getBlockModelShapes().registerBlockWithStateMapper(this, stateMapBuilder.build());
        }
    }

    @Override
    public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List list, Entity collidingEntity) {
        if (!reduxBlock.hasMultipleCollisionBoxes()) {
            super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
        } else {
            for (Block.CollisionBox collisionBox : reduxBlock.getCollisionBoxes()) {
                this.setBlockBounds(collisionBox.getMinX(), collisionBox.getMinY(), collisionBox.getMinZ(),
                        collisionBox.getMaxX(), collisionBox.getMaxY(), collisionBox.getMaxZ());
                super.addCollisionBoxesToList(worldIn, pos, state, mask, list, collidingEntity);
            }
            this.setBlockBoundsForItemRender();
        }
    }

    @Override
    public boolean isCollidable() {
        return reduxBlock.getCollisionBoxes() != null && reduxBlock.getCollisionBoxes().isEmpty();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return reduxBlock.tickable() || !reduxBlock.getScript().isEmpty();
    }

    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, Entity entityIn) {
        if (worldIn.getTileEntity(pos) instanceof ReduxCommandBlockTileEntity) {
            ReduxCommandBlockTileEntity commandBlockTileEntity = (ReduxCommandBlockTileEntity) worldIn.getTileEntity(pos);
            commandBlockTileEntity.triggerSpecialEvent(Trigger.TriggerEvent.OnEntityCollide, entityIn);
        }
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return createNewTileEntity(world, 0);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        ReduxCommandBlockTileEntity te = null;

        if (reduxBlock.tickable()) {
            te = new ReduxCommandBlockTickableTileEntity();
        } else if (!reduxBlock.getScript().isEmpty()) {
            te = new ReduxCommandBlockTileEntity();
        }

        // Do initial initialization of the tile entity
        if (te != null)
            te.init(pack.getId(), reduxBlock);

        return te;
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return hasTileEntity(null);
    }

    @Override
    public int getComparatorInputOverride(World worldIn, BlockPos pos) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity instanceof ReduxCommandBlockTileEntity ? ((ReduxCommandBlockTileEntity)tileentity).getLastSuccessCount() > 15
                ? 15 : ((ReduxCommandBlockTileEntity)tileentity).getLastSuccessCount(): 0;
    }

    public PropertyInteger getPropertyFromName(String name) {
        return customBlockProperties.get(name);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        if (hasTileEntity(state) && !state.getPropertyNames().isEmpty()) {
            return (Integer)state.getValue(SUCCESS_COUNT_META);
        }
        return 0;
    }

    public Class<? extends ReduxCommandBlockTileEntity> getTileEntityClass() {
        if (reduxBlock.tickable())
            return ReduxCommandBlockTickableTileEntity.class;
        if (!reduxBlock.getScript().isEmpty())
            return ReduxCommandBlockTileEntity.class;
        return null;
    }

    @Override
    protected BlockState createBlockState() {
        Block reduxBlock;
        if (this.reduxBlock == null)
            reduxBlock = blockThreadLocal.get();
        else
            reduxBlock = this.reduxBlock;
        if (reduxBlock == null) throw new AssertionError();

        if (reduxBlock.shouldAddFacingProperty())
            return new BlockState(this, SUCCESS_COUNT_META, FACING);
        else
            return new BlockState(this, SUCCESS_COUNT_META);
    }

    @Override
    public boolean isOpaqueCube() {
        Block reduxBlock;
        if (this.reduxBlock == null)
            reduxBlock = blockThreadLocal.get();
        else
            reduxBlock = this.reduxBlock;
        if (reduxBlock == null) throw new AssertionError();

        return reduxBlock.isFullCube();
    }

    @Override
    public boolean isFullCube() {
        Block reduxBlock;
        if (this.reduxBlock == null)
            reduxBlock = blockThreadLocal.get();
        else
            reduxBlock = this.reduxBlock;
        if (reduxBlock == null) throw new AssertionError();

        return reduxBlock.isFullCube();
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, net.minecraft.block.Block neighborBlock) {
        if (!worldIn.isRemote && !this.canPlaceBlockAt(worldIn, pos) && reduxBlock.isWeak()) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
        }

        boolean redstoneChanged = false;
        for (EnumFacing dir : EnumFacing.values()) {
            if (lastRedstoneStrongStrength[dir.getIndex()] != worldIn.getRedstonePower(pos, dir)) {
                lastRedstoneStrongStrength[dir.getIndex()] = worldIn.getRedstonePower(pos, dir);
                redstoneChanged = true;
            }
        }
        if (lastRedstoneWeakStrength != worldIn.isBlockIndirectlyGettingPowered(pos)) {
            lastRedstoneWeakStrength = worldIn.isBlockIndirectlyGettingPowered(pos);
            redstoneChanged = true;
        }

        if (redstoneChanged && worldIn.getTileEntity(pos) instanceof ReduxCommandBlockTileEntity) {
            ReduxCommandBlockTileEntity commandBlockTileEntity = (ReduxCommandBlockTileEntity) worldIn.getTileEntity(pos);
            commandBlockTileEntity.triggerSpecialEvent(Trigger.TriggerEvent.OnRestoneStrengthChange, lastRedstoneStrongStrength, lastRedstoneWeakStrength);
        }
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return reduxBlock.isWeak() ? (World.doesBlockHaveSolidTopSurface(worldIn, pos.down()) || worldIn.getBlockState(pos.down()).getBlock() == Blocks.glowstone)
                : super.canPlaceBlockAt(worldIn, pos);
    }

    /**
     * Left click?
     * @param worldIn The world
     * @param pos The position of the block
     * @param playerIn The player whom clicked
     */
    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
        if (worldIn.getTileEntity(pos) instanceof ReduxCommandBlockTileEntity) {
            ReduxCommandBlockTileEntity commandBlockTileEntity = (ReduxCommandBlockTileEntity) worldIn.getTileEntity(pos);
            commandBlockTileEntity.triggerSpecialEvent(Trigger.TriggerEvent.OnEntityCollide, false, playerIn);
        }
    }

    // Right click?
    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (worldIn.getTileEntity(pos) instanceof ReduxCommandBlockTileEntity) {
            ReduxCommandBlockTileEntity commandBlockTileEntity = (ReduxCommandBlockTileEntity) worldIn.getTileEntity(pos);
            return commandBlockTileEntity.triggerSpecialEvent(Trigger.TriggerEvent.OnEntityCollide, true, playerIn, side, hitX, hitY, hitZ);
        }
        return false;
    }

    @Override
    public boolean canProvidePower() {
        return reduxBlock.getRedstoneOutputProperty() != null && !reduxBlock.getRedstoneOutputProperty().isEmpty();
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side) {
        if (customBlockProperties.get(reduxBlock.getRedstoneOutputProperty()) != null) {
            PropertyInteger redstoneProperty = customBlockProperties.get(reduxBlock.getRedstoneOutputProperty());
            return ((Integer)state.getValue(redstoneProperty)) > 15 ? 15 : ((Integer)state.getValue(redstoneProperty));
        }
        return 0;
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side) {
        if (customBlockProperties.get(reduxBlock.getRedstoneOutputProperty()) != null) {
            PropertyInteger redstoneProperty = customBlockProperties.get(reduxBlock.getRedstoneOutputProperty());
            return ((Integer)state.getValue(redstoneProperty)) > 15 ? 15 : ((Integer)state.getValue(redstoneProperty));
        }
        return 0;
    }

    /**
     * Always render the item with full bounds.
     * Andrew Querol: I cannot think of a need for anything else.
     */
    @Override
    public void setBlockBoundsForItemRender() {
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }
}