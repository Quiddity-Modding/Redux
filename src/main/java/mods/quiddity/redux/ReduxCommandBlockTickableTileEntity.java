package mods.quiddity.redux;

import mods.quiddity.redux.json.model.Block;
import net.minecraft.server.gui.IUpdatePlayerListBox;

import java.util.HashSet;
import java.util.Set;

/**
 * The extension that adds a OnTick event.
 *
 * @author winsock on 2/5/15.
 */
public class ReduxCommandBlockTickableTileEntity extends ReduxCommandBlockTileEntity implements IUpdatePlayerListBox {

    private Set<ReduxBlockEventReceiver> tickEventReceivers = new HashSet<ReduxBlockEventReceiver>();
    private int ticks = 0;

    public ReduxCommandBlockTickableTileEntity(Block block) {
        super(block);
    }

    public void addTickEventReceiver(ReduxBlockEventReceiver receiver) {
        tickEventReceivers.add(receiver);
    }

    @Override
    public void update() {
        ticks++;
        if (ticks >= reduxBlock.getTickRate()) {
            for (ReduxBlockEventReceiver receiver : tickEventReceivers) {
                receiver.receiveEvent(null);
            }
            ticks = 0;
        }
    }
}
