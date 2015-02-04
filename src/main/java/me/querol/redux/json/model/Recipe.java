package me.querol.redux.json.model;

import java.util.Set;

/**
 * JSON Object model that represents all Redux pack recipies
 *
 * @author winsock on 1/23/15.
 */
@SuppressWarnings("all")
public class Recipe {
    private String held;
    private String target;
    private String result;
    private Set<String> drops;

    public String getHeld() {
        return held;
    }

    public void setHeld(String held) {
        this.held = held;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Set<String> getDrops() {
        return drops;
    }

    public void setDrops(Set<String> drops) {
        this.drops = drops;
    }
}
