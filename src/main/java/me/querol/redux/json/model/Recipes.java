package me.querol.redux.json.model;

import java.util.List;

/**
 * Created by winsock on 1/23/15.
 */
public class Recipes {
    private List<Recipe> recipes;

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
    }
}
