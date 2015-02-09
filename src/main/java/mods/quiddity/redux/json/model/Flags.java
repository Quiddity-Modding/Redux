package mods.quiddity.redux.json.model;

import javax.annotation.Nonnull;

/**
 * JSON Object model to represent Redux configuration features that are key value based.
 *
 * {@link KT} The type of the key
 * {@link VT} The type of the value
 *
 * @author winsock on 2/5/15.
 */
@SuppressWarnings("all")
public class Flags<KT, VT> {
    /**
     * The key
     */
    @Nonnull
    protected KT key;

    /**
     * The value
     */
    @Nonnull
    protected VT value;

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
