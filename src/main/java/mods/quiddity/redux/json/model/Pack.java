package mods.quiddity.redux.json.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * JSON Object model to represent Redux packs.
 *
 * @author winsock on 2/3/15.
 */
@SuppressWarnings("all")
public class Pack {
    @Nonnull
    private String id;
    @Nonnull
    private String name;
    @Nullable
    private String author;
    @Nullable
    private String description;

    @Nullable
    private List<Block> block_list;
    @Nullable
    private List<Item> items;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public List<Block> getBlocks() {
        return block_list == null ? null : ImmutableList.copyOf(block_list);
    }

    public List<Item> getItems() {
        return items == null ? null : ImmutableList.copyOf(items);
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
