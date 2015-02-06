package mods.quiddity.redux;

import net.minecraft.server.gui.IUpdatePlayerListBox;

/**
 * The extension that adds a OnTick event.
 *
 * @author winsock on 2/5/15.
 */
public class ReduxCommandBlockTickableTileEntity extends ReduxCommandBlockTileEntity implements IUpdatePlayerListBox {
    private int ticks = 0;

    public ReduxCommandBlockTickableTileEntity() {}

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
