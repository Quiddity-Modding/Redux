package me.querol.redux.json.model;

/**
 * Created by winsock on 1/23/15.
 */
public class Block {
    private String name;
    private String description;
    private String id;

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

    @Override
    public String toString() {
        return name + " - " + description;
    }
}