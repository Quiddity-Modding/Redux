package mods.quiddity.redux.json.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * JSON Object model that represents all Redux pack blocks.
 *
 * @author winsock on 1/23/15.
 */
@SuppressWarnings("all")
public class Block {
    /**
     * If this block should extend another block.
     * {@link mods.quiddity.redux.json.model.Block}
     */
    @Nullable
    protected String extendsBlock;
    private transient Block reduxExtendsBlock = null;

    /**
     * <h1>Required</h1>
     * The id of the block. Should not conflict with any other {@link mods.quiddity.redux.json.model.Block} of the {@link mods.quiddity.redux.json.model.Pack}
     * <em>Should contain no spaces or special characters.</em>
     * <em>ASCII</em>
     */
    @Nonnull
    protected String id;

    /**
     * <h1>Required</h1>
     * The human readable name of the block.
     */
    @Nonnull
    protected String name;

    /**
     * <h1>Required</h1>
     * The material of the block.
     * The valid properties are:
     * <ul>
     *     <li>air</li>
     *     <li>grass</li>
     *     <li>ground</li>
     *     <li>wood</li>
     *     <li>rock</li>
     *     <li>iron</li>
     *     <li>anvil</li>
     *     <li>water</li>
     *     <li>lava</li>
     *     <li>leaves</li>
     *     <li>plants</li>
     *     <li>vine</li>
     *     <li>sponge</li>
     *     <li>cloth</li>
     *     <li>fire</li>
     *     <li>sand</li>
     *     <li>circuits</li>
     *     <li>carpet</li>
     *     <li>glass</li>
     *     <li>redstoneLight</li>
     *     <li>tnt</li>
     *     <li>coral</li>
     *     <li>ice</li>
     *     <li>packedIce</li>
     *     <li>snow</li>
     *     <li>craftedSnow</li>
     *     <li>cactus</li>
     *     <li>clay</li>
     *     <li>gourd</li>
     *     <li>dragonEgg</li>
     *     <li>portal</li>
     *     <li>cake</li>
     *     <li>web</li>
     *     <li>piston</li>
     *     <li>barrier</li>
     * </ul>
     */
    @Nonnull
    protected String material;

    @Nullable
    protected String description;

    /**
     * Is it a full cube? I.E. should it render transparency.
     * Defaults to true
     * Or does the cube not fill the whole block?
     * <em>Cannot be inherited</em>
     */
    @Nullable
    protected boolean full_cube = true;

    /**
     * Does the block break like redstone when water/lava flow over it.
     * <em>Cannot be inherited</em>
     */
    @Nullable
    protected boolean is_weak;

    /**
     * A list of collision boxes to add.
     * An empty list means that the block is uncollidable.
     * The absence of the collison_boxes property indicates to use the default full block collision.
     */
    @Nullable
    protected List<CollisionBox> collisionBoxes;

    /**
     * Does this block use the ‘facing’ property to change textures/”rotate” the block.
     * <em>Cannot be inherited</em>
     */
    @Nullable
    protected boolean directional;

    /**
     * Extra properties that are used either in the scripts or for model rendering. They are integer properties.
     * <em>These must be <b>INTEGER</b> properties</em>
     */
    @Nullable
    protected List<Flags<String, Integer>> custom_properties;

    /**
     * Properties to ignore when rendering.
     * I.E. Properties that do not affect the look of the block.
     */
    @Nullable
    protected List<String> ignored_properties;

    /**
     * The property name to use for redstone output.
     * If this property is null or non-existent then the block will not emit any redstone.
     */
    @Nullable
    protected String redstone_output_property;

    /**
     * Name of vanilla or new tab.
     */
    @Nullable
    protected String creative_tab;

    /**
     * The name/id of the item to show on the tab if it’s a new tab only.
     * Format: ‘domain:item’ I.E. ‘minecraft:stick’
     */
    @Nullable
    protected String creative_tab_icon;

    /**
     * The tick rate of the block. 0 Means no ticking.
     * <em>Without this properity set no {@link mods.quiddity.redux.json.model.Trigger.TriggerEvent}.OnTick events will be called</em>
     */
    @Nullable
    protected int tick_rate = -1;

    /**
     * List of names for the ore dictionary to recognize the block as for crafting.
     * <em>Not Implemented Yet</em>
     */
    @Nullable
    protected List<String> ore_dictionary;

    /**
     * Block scripting using command block logic.
     * {@link mods.quiddity.redux.json.model.Trigger}
     * {@link mods.quiddity.redux.json.model.Trigger.TriggerEvent}
     */
    @Nullable
    protected List<Trigger> script;

    private transient CreativeTabs creativeTabObject = null;

    public String getExtendsBlock() {
        return extendsBlock;
    }

    public Block getReduxExtendsBlock() {
        return reduxExtendsBlock;
    }

    public void setReduxExtendsBlock(Block reduxExtendsBlock) {
        this.reduxExtendsBlock = reduxExtendsBlock;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description == null ? reduxExtendsBlock == null ? "" : reduxExtendsBlock.getDescription() : description;
    }

    public String getId() {
        return id;
    }

    public int getTickRate() {
        return tick_rate == -1 ? reduxExtendsBlock == null ? 0 : reduxExtendsBlock.getTickRate() : tick_rate;
    }

    public boolean tickable() {
        return getTickRate() > 0;
    }

    public Material getMaterial() {
        if (material == null && reduxExtendsBlock != null) {
            return reduxExtendsBlock.getMaterial();
        }
        try {
            return (Material) Material.class.getField(material).get(null);
        } catch (Exception e) {
            return Material.air;
        }
    }

    public boolean shouldAddFacingProperty() {
        return directional;
    }

    public List<CollisionBox> getCollisionBoxes() {
        if (collisionBoxes != null)
            return ImmutableList.copyOf(collisionBoxes);
        if (reduxExtendsBlock != null)
            return reduxExtendsBlock.getCollisionBoxes();
        return null;
    }

    public boolean hasMultipleCollisionBoxes() {
        return getCollisionBoxes() != null && getCollisionBoxes().size() > 1;
    }

    public boolean isFullCube() {
        return full_cube;
    }

    public boolean isWeak() {
        return is_weak;
    }

    public CreativeTabs getCreativeTab() {
        if (creative_tab == null || creative_tab.isEmpty()) {
            if (reduxExtendsBlock != null && reduxExtendsBlock.getCreativeTab() != CreativeTabs.tabAllSearch)
                return reduxExtendsBlock.getCreativeTab();
            return creativeTabObject = CreativeTabs.tabAllSearch;
        } else if (creativeTabObject == null) {
            for (CreativeTabs tab : CreativeTabs.creativeTabArray) {
                if (tab.getTabLabel().equalsIgnoreCase(creative_tab))
                    creativeTabObject = tab;
            }
            if (creativeTabObject == null) {
                creativeTabObject = new CreativeTabs(creative_tab) {
                    @Override
                    public Item getTabIconItem() {
                        return (creative_tab_icon == null || creative_tab_icon.isEmpty() || Item.getByNameOrId(creative_tab_icon) == null)
                                ? reduxExtendsBlock == null ? ItemBlock.getItemFromBlock(Blocks.air) : reduxExtendsBlock.creative_tab_icon == null ?
                                ItemBlock.getItemFromBlock(Blocks.air) : Item.getByNameOrId(reduxExtendsBlock.creative_tab_icon) : Item.getByNameOrId(creative_tab_icon);
                    }
                };
            }
        }

        return creativeTabObject;
    }

    public List<Trigger> getScript() {
        if (script == null && reduxExtendsBlock != null && reduxExtendsBlock.getScript() != null)
            return reduxExtendsBlock.getScript();
        if (script == null)
            return Collections.EMPTY_LIST;
        return ImmutableList.copyOf(script);
    }

    public List<String> getOreDictionaryNames() {
        if (ore_dictionary == null && reduxExtendsBlock != null && reduxExtendsBlock.getOreDictionaryNames() != null)
            return reduxExtendsBlock.getOreDictionaryNames();
        if (ore_dictionary == null)
            return null;
        return ImmutableList.copyOf(ore_dictionary);
    }

    public List<Flags<String, Integer>> getCustomProperties() {
        if (custom_properties == null && reduxExtendsBlock != null && reduxExtendsBlock.getCustomProperties() != null)
            return reduxExtendsBlock.getCustomProperties();
        if (custom_properties == null)
            return null;
        return ImmutableList.copyOf(custom_properties);
    }

    public List<String> getIgnoredProperties() {
        if (ignored_properties == null && reduxExtendsBlock != null && reduxExtendsBlock.getIgnoredProperties() != null)
            return reduxExtendsBlock.getIgnoredProperties();
        if (ignored_properties == null)
            return null;
        return ImmutableList.copyOf(ignored_properties);
    }

    public String getRedstoneOutputProperty() {
        return redstone_output_property == null ? reduxExtendsBlock == null ? "" : reduxExtendsBlock.getRedstoneOutputProperty() : redstone_output_property;
    }

    @Override
    public String toString() {
        return id + " - " + description;
    }

    public class CollisionBox {
        private float minX, minY, minZ;
        private float maxX, maxY, maxZ;

        public float getMinX() {
            return minX;
        }

        public float getMinY() {
            return minY;
        }

        public float getMinZ() {
            return minZ;
        }

        public float getMaxX() {
            return maxX;
        }

        public float getMaxY() {
            return maxY;
        }

        public float getMaxZ() {
            return maxZ;
        }
    }
}