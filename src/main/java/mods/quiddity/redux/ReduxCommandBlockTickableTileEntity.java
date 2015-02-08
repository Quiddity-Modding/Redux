package mods.quiddity.redux;

import mods.quiddity.redux.json.model.Trigger;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

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
        if (worldObj.isRemote)
            return;
        ticks++;
        if (ticks >= reduxBlock.getTickRate()) {
            FMLCommonHandler.callFuture(new FutureTask<Void>(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    triggerSpecialEvent(Trigger.TriggerEvent.OnTick);
                    return null;
                }
            }));
            ticks = 0;
        }
    }
}
