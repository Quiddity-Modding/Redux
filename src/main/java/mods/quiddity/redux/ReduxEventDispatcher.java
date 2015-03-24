package mods.quiddity.redux;

import mods.quiddity.redux.json.model.Pack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.script.ScriptException;
import java.lang.reflect.Field;

/**
 * WeakReference event dispatcher. This handles firing all of the events for Redux blocks.
 * This only holds weak references to the TileEntity. This should allow it to properly unload.
 *
 * @author winsock on 2/7/15.
 */
public class ReduxEventDispatcher {
    private static ReduxEventDispatcher ourInstance = null;

/*    private final Map<Trigger.TriggerEvent, List<WeakReference<ReduxCommandBlockTileEntity.ReduxBlockEventReceiver>>> eventListMap
            = new WeakHashMap<Trigger.TriggerEvent, List<WeakReference<ReduxCommandBlockTileEntity.ReduxBlockEventReceiver>>>();*/

    public static ReduxEventDispatcher getInstance() {
        if (ourInstance == null)
            ourInstance = new ReduxEventDispatcher();
        return ourInstance;
    }

    private ReduxEventDispatcher() {
        MinecraftForge.EVENT_BUS.register(this);
    }

/*
    public void registerEventReceiver(ReduxCommandBlockTileEntity.ReduxBlockEventReceiver receiver) {
        if (eventListMap.get(receiver.getTriggerScript().getTriggerEvent()) == null)
            eventListMap.put(receiver.getTriggerScript().getTriggerEvent(), new ArrayList<WeakReference<ReduxCommandBlockTileEntity.ReduxBlockEventReceiver>>());
        eventListMap.get(receiver.getTriggerScript().getTriggerEvent()).add(new WeakReference<ReduxCommandBlockTileEntity.ReduxBlockEventReceiver>(receiver));
    }
*/

    @SubscribeEvent
    public void onEvent(Event event) {
/*        if (eventListMap.get(Trigger.TriggerEvent.getTriggerEventFromForgeEvent(event.getClass())) != null) {
            List<WeakReference<ReduxCommandBlockTileEntity.ReduxBlockEventReceiver>> weakReferences =
                    ImmutableList.copyOf(eventListMap.get(Trigger.TriggerEvent.getTriggerEventFromForgeEvent(event.getClass())));
            for (WeakReference<ReduxCommandBlockTileEntity.ReduxBlockEventReceiver> eventReceiverWeakReference : weakReferences) {
                ReduxCommandBlockTileEntity.ReduxBlockEventReceiver eventReceiver = eventReceiverWeakReference.get();
                if (eventReceiver == null) {
                    eventListMap.get(Trigger.TriggerEvent.getTriggerEventFromForgeEvent(event.getClass())).remove(eventReceiverWeakReference);
                    continue;
                }
                eventReceiver.receiveEvent(event);
            }
        }*/
        if (FMLCommonHandler.instance().getMinecraftServerInstance() != null &&
                FMLCommonHandler.instance().getMinecraftServerInstance().isCallingFromMinecraftThread()) {
            for (Pack p : Redux.instance.getReduxConfiguration().getPacks()) {
                if (p.getJsEngine().getEngine().hasObject(event.getClass().getSimpleName())) {
                    try {
                        if (event.getClass().getField("world") != null) {
                            Field worldField = event.getClass().getField("world");
                            worldField.setAccessible(true);
                            p.getJsEngine().getEngine().addJavaObject("world", worldField.get(event));
                        }
                    } catch (Exception ignored) {}
                    try {
                        if (event.getClass().getField("entity") != null) {
                            Field worldField = event.getClass().getField("entity");
                            worldField.setAccessible(true);
                            p.getJsEngine().getEngine().addJavaObject("entity", worldField.get(event));
                        }
                    } catch (Exception ignored) {}
                    try {
                        if (event.getClass().getField("pos") != null) {
                            Field worldField = event.getClass().getField("pos");
                            worldField.setAccessible(true);
                            p.getJsEngine().getEngine().addJavaObject("pos", worldField.get(event));
                        }
                    } catch (Exception ignored) {}

                    try {

                        p.getJsEngine().getEngine().callMethod(event.getClass().getSimpleName(), event);

                    } catch (ScriptException e) {
                        Redux.instance.getLogger().warn("Redux pack inconsistency. A script file in pack: %s has errors.", p.getName());
                    } catch (NoSuchMethodException e) {
                        Redux.instance.getLogger().warn("Redux pack inconsistency. A script file in pack: %s has errors.", p.getName());
                    }
                }
            }
        }
    }
}
