package mods.quiddity.redux;

import net.minecraft.client.Minecraft;

public class ReduxClientProxy extends ReduxCommonProxy {
    @Override
    public boolean isSinglePlayer() {
        return Minecraft.getMinecraft().isSingleplayer();
    }
}
