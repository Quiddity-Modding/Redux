package mods.quiddity.redux.json.model;

/**
 * JSON Object model that represents all Redux pack blocks.
 *
 * @author winsock on 1/23/15.
 */
@SuppressWarnings("all")
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