package me.querol.redux.json.model;

import java.util.List;

/**
 * JSON Object model to represent Redux packs.
 *
 * @author winsock on 2/3/15.
 */
@SuppressWarnings("all")
public class Pack {
    private String name;
    private String id;

    private List<Block> blocks;
    private List<Recipe> recipes;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
    public List<Block> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
    }

    @Override
    public String toString() {
        return getName();
    }
}
