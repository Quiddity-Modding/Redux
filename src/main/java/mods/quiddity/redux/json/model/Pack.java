package mods.quiddity.redux.json.model;

import java.util.List;

/**
 * JSON Object model to represent Redux packs.
 *
 * @author winsock on 2/3/15.
 */
@SuppressWarnings("all")
public class Pack {
    private String id;
    private String name;
    private String author;
    private String description;

    private List<Block> blocks;
    private List<Item> items;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public List<Item> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return getName();
    }
}
