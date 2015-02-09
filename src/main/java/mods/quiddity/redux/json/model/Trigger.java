package mods.quiddity.redux.json.model;

import com.google.common.collect.ImmutableList;
import mods.quiddity.redux.Redux;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * JSON Object model to represent Redux event triggers
 *
 * @author winsock on 2/5/15.
 */
@SuppressWarnings("all")
public class Trigger {

    /**
     * <h1>Required</h1>
     * The name of the event to trigger on.
     * See: {@link mods.quiddity.redux.json.model.Trigger.TriggerEvent}
     */
    @Nonnull
    protected String trigger;

    /**
     * Trigger only when this block is the target of the event.
     * Defaults to true when this flag is not present.
     */
    @Nullable
    protected boolean on_cause = true;

    /**
     * <h1>Required</h1>
     * Commands are an array of commands to execute in sequence.
     * Result of prior commands pushed onto a stack as well.
     * If a command has $(POP) it will pop off the last result on the stack and insert it.
     * If it has $(PEEK) it will peek at the top result and insert the value.
     * The value is not removed from the stack with peek.
     * You can pop/peek multiple times, it will pop from left to right.
     */
    @Nonnull
    protected List<String> commands;

    public TriggerEvent getTriggerEvent() {
        if (trigger == null || trigger.isEmpty()) {
            Redux.instance.getLogger().warn("A block tried to register a trigger that is either empty or null.");
            return null;
        }
        return TriggerEvent.valueOf(trigger);
    }

    public List<String> getCommands() {
        if (commands == null || commands.isEmpty()) {
            Redux.instance.getLogger().warn("A block tried to register a trigger that has no commands.");
            return null;
        }
        return ImmutableList.copyOf(commands);
    }

    public boolean blockHasToBeTheCause() {
        return on_cause;
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
        OnInteract(Event.class),
        OnRestoneStrengthChange(Event.class);

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