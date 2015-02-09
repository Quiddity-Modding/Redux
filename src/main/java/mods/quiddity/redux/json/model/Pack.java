package mods.quiddity.redux.json.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON Object model to represent Redux packs.
 *
 * @author winsock on 2/3/15.
 */
@SuppressWarnings("all")
public class Pack {
    /**
     * <h1>Required</h1>
     * The mod id of the pack. Must not conflict with any other {@link net.minecraftforge.fml.common.FMLModContainer}.
     * <em>Should contain no spaces or special characters.</em>
     * <em>ASCII</em>
     */
    @Nonnull
    protected String id;

    /**
     * <h1>Required</h1>
     * The human readable name of the mod.
     * Will be displayed as a child-mod of Redux in the mods list
     */
    @Nonnull
    protected String name;

    /**
     * The author of the Redux pack.
     * <em>Curently doesn't display anywhere</em>
     */
    @Nullable
    protected String author;

    /**
     * The author's description of the Redux pack.
     * <em>Curently doesn't display anywhere</em>
     */
    @Nullable
    protected String description;

    /**
     * A list of blocks to load.
     * Not <i>required</i> however the pack will not have any new blocks without them
     * {@link mods.quiddity.redux.json.model.Block}
     */
    @Nullable
    protected transient List<Block> block_list;

    /**
     * A list of items to load.
     * Not <i>required</i> however the pack will not have any new items without them
     * {@link mods.quiddity.redux.json.model.Item}
     *
     * <em>Unimplemented</em>
     */
    @Nullable
    protected List<Item> item_list;

    private transient Map<String, Block> idMap = null;
    private transient boolean hasAddedBlocks = false;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public List<Block> getBlocks() {
        return block_list == null ? null : block_list;
    }

    public void setBlockList(List<Block> blocks) {
        if (!hasAddedBlocks) {
            block_list = ImmutableList.copyOf(blocks);
            hasAddedBlocks = true;
        }
    }

    public Block getBlockFromId(String id) {
        if (getBlocks() == null || id == null)
            return null;
        if (idMap == null) {
            idMap = new HashMap<String, Block>();
            for (Block b : getBlocks()) {
                idMap.put(b.getId(), b);
            }
        }
        return idMap.get(id);
    }

    public List<Item> getItems() {
        return item_list == null ? null : ImmutableList.copyOf(item_list);
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return getName();
    }
}
