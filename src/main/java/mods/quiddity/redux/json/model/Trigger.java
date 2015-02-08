package mods.quiddity.redux.json.model;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.List;

/**
 * JSON Object model to represent Redux event triggers
 *
 * @author winsock on 2/5/15.
 */
@SuppressWarnings("all")
public class Trigger {
    private String trigger;
    private List<String> commands;

    public TriggerEvent getTriggerEvent() {
        return TriggerEvent.valueOf(trigger);
    }

    public List<String> getCommands() {
        return ImmutableList.copyOf(commands);
    }

    public enum TriggerEvent {
        BlockHarvestDropsEvent(BlockEvent.HarvestDropsEvent.class),
        BlockBreakEvent(BlockEvent.BreakEvent.class),
        BlockPlaceEvent(BlockEvent.PlaceEvent.class),
        BlockMultiPlaceEvent(BlockEvent.MultiPlaceEvent.class),
        WorldLoad(WorldEvent.Load.class),
        WorldUnload(WorldEvent.Unload.class),
        WorldPotentialSpawns(WorldEvent.PotentialSpawns.class),
        WorldCreateSpawnPosition(WorldEvent.CreateSpawnPosition.class),
        ExplosionStart(ExplosionEvent.Start.class),
        ExplosionDetonate(ExplosionEvent.Detonate.class),
        ChunkWatch(ChunkWatchEvent.Watch.class),
        ChunkUnWatch(ChunkWatchEvent.UnWatch.class),
        ServerChatEvent(net.minecraftforge.event.ServerChatEvent.class),
        OnTick(Event.class),
        OnEntityCollide(Event.class),
        OnInteract(Event.class);

        private Class<? extends Event> forgeEventClass;
        private TriggerEvent(Class<? extends Event> forgeEvent) {
            this.forgeEventClass = forgeEvent;
        }

        public Class<? extends Event> getForgeEventClass() {
            return forgeEventClass;
        }

        public static TriggerEvent getTriggerEventFromForgeEvent(Class<? extends Event> forgeEvent) {
            for (TriggerEvent e: TriggerEvent.values()) {
                if (e.forgeEventClass == forgeEvent) {
                    return e;
                }
            }
            return null;
        }
    }
}