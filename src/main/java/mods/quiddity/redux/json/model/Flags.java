package mods.quiddity.redux.json.model;

/**
 * JSON Object model to represent Redux configuration features that are key value based
 *
 * @author winsock on 2/5/15.
 */
@SuppressWarnings("all")
public class Flags<KT, VT> {
    private KT key;
    private VT value;

    public Flags() { }

    public Flags(KT key, VT value) {
        this.key = key;
        this.value = value;
    }

    public KT getKey() {
        return key;
    }

    public VT getValue() {
        return value;
    }
}
