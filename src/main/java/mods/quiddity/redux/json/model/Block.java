package mods.quiddity.redux.json.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import java.util.List;

/**
 * JSON Object model that represents all Redux pack blocks.
 *
 * @author winsock on 1/23/15.
 */
@SuppressWarnings("all")
public class Block {
    private String id;
    private String name;
    private String description;

    private String material;
    private boolean full_cube;
    private List<CollisionBox> collisionBoxes;
    private boolean directional;
    private List<Flags<String, Integer>> custom_properties;
    private String creative_tab;
    private String creative_tab_icon;
    private int tick_rate;
    private List<String> ore_dictionary;

    private List<Trigger> script;

    private transient CreativeTabs creativeTabObject = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public CreativeTabs getCreativeTab() {
        if (creativeTabObject == null) {
            for (CreativeTabs tab : CreativeTabs.creativeTabArray) {
                if (tab.getTabLabel().equalsIgnoreCase(creative_tab))
                    creativeTabObject = tab;
            }

            if (creativeTabObject == null) {
                creativeTabObject = new CreativeTabs(creative_tab) {
                    @Override
                    public Item getTabIconItem() {
                        return Item.getByNameOrId(creative_tab_icon);
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