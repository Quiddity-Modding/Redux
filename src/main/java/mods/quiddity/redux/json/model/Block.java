package mods.quiddity.redux.json.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * JSON Object model that represents all Redux pack blocks.
 *
 * @author winsock on 1/23/15.
 */
@SuppressWarnings("all")
public class Block {
    @Nonnull
    private String id;
    @Nonnull
    private String name;
    @Nonnull
    private String description;
    @Nonnull
    private String material;

    @Nullable
    private boolean full_cube, is_weak;
    @Nullable
    private List<CollisionBox> collisionBoxes;
    @Nullable
    private boolean directional;
    @Nullable
    private List<Flags<String, Integer>> custom_properties;
    @Nullable
    private List<String> ignored_properties;
    @Nullable
    private String redstone_output_property;
    @Nullable
    private String creative_tab;
    @Nullable
    private String creative_tab_icon;
    @Nullable
    private int tick_rate;
    @Nullable
    private List<String> ore_dictionary;
    @Nullable
    private List<Trigger> script;

    private transient CreativeTabs creativeTabObject = null;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public String getId() {
        return id;
    }

    public int getTickRate() {
        return tick_rate;
    }

    public boolean tickable() {
        return tick_rate > 0;
    }

    public Material getMaterial() {
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
        return null;
    }

    public boolean hasMultipleCollisionBoxes() {
        return collisionBoxes != null && collisionBoxes.size() > 1;
    }

    public boolean isFullCube() {
        return full_cube;
    }

    public boolean isWeak() {
        return is_weak;
    }

    public CreativeTabs getCreativeTab() {
        if (creative_tab == null || creative_tab.isEmpty()) {
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
                                ? ItemBlock.getItemFromBlock(Blocks.air) : Item.getByNameOrId(creative_tab_icon);
                    }
                };
            }
        }

        return creativeTabObject;
    }

    public List<Trigger> getScript() {
        return ImmutableList.copyOf(script);
    }

    public List<String> getOreDictionaryNames() {
        return ImmutableList.copyOf(ore_dictionary);
    }

    public List<Flags<String, Integer>> getCustomProperties() {
        if (custom_properties == null)
            return null;
        return ImmutableList.copyOf(custom_properties);
    }

    public List<String> getIgnoredProperties() {
        if (ignored_properties == null)
            return null;
        return ImmutableList.copyOf(ignored_properties);
    }

    public String getRedstoneOutputProperty() {
        return redstone_output_property == null ? "" : redstone_output_property;
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