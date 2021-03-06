package mods.quiddity.redux;

import mods.quiddity.redux.json.model.Block;
import mods.quiddity.redux.json.model.Pack;
import mods.quiddity.redux.json.model.Trigger;
import net.minecraft.command.CommandResultStats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import java.text.NumberFormat;
import java.text.ParsePosition;

/**
 * The tile entity for Redux Pack blocks that have custom command scripts.
 *
 * @author winsock on 2/5/15.
 */
public class ReduxCommandBlockTileEntity extends TileEntity {

    private String packId = "";
    private Object[] lastEventArgs;

    protected volatile Block reduxBlock = null;
    protected int lastSuccessCount = 0;
    protected CommandResultStats.Type lastResultType = CommandResultStats.Type.SUCCESS_COUNT;
    protected int lastResultAmount = 0;

/*    protected final Set<ReduxBlockEventReceiver> eventReceivers = new HashSet<ReduxBlockEventReceiver>();
    protected final Map<Trigger.TriggerEvent, Set<ReduxBlockEventReceiver>> specialReceivers = new HashMap<Trigger.TriggerEvent, Set<ReduxBlockEventReceiver>>();
    protected final Map<String, String> reduxVariables = new HashMap<String, String>();
    protected final Stack<Integer> commandResultStack = new Stack<Integer>();*/

    public ReduxCommandBlockTileEntity() {}

    public int getLastSuccessCount() {
        return lastSuccessCount;
    }

/*    public void addSpecialEventReceiver(Trigger.TriggerEvent event, ReduxBlockEventReceiver receiver) {
        if (!specialReceivers.containsKey(event))
            specialReceivers.put(event, new HashSet<ReduxBlockEventReceiver>());
        specialReceivers.get(event).add(receiver);
    }*/

    public void init(String packId, Block reduxBlock) {
        this.packId = packId;
        this.reduxBlock = reduxBlock;
        setupTriggers();
    }

    public void setupTileEntity(String blockId) {
        Pack p = Redux.instance.getReduxConfiguration().getPackFromId(packId);
        if (p == null) throw new AssertionError();
        reduxBlock = p.getBlockFromId(blockId);
        if (reduxBlock == null) throw new AssertionError();

/*        specialReceivers.clear();
        eventReceivers.clear();*/
        setupTriggers();
    }

    private void setupTriggers() {
        /*for (Trigger trigger : reduxBlock.getScript()) {
            // We have to keep a local strong reference. Otherwise GC would remove our event receiver right away.
            ReduxBlockEventReceiver receiver = new ReduxBlockEventReceiver(trigger);
            if (trigger.getTriggerEvent().getForgeEventClass() != Event.class) {
                eventReceivers.add(receiver);
                ReduxEventDispatcher.getInstance().registerEventReceiver(receiver);
            } else {
                addSpecialEventReceiver(trigger.getTriggerEvent(), receiver);
            }
        }*/
    }

    /**
     * Call listening special events.
     * @param event The event that was triggered
     * @param args Any extra data to pass on about the event
     * @return Were any receiversCalled
     */
    public boolean triggerSpecialEvent(Trigger.TriggerEvent event, Object... args) {
        /*if (specialReceivers.containsKey(event)) {
            lastEventArgs = args;
            for (ReduxBlockEventReceiver eventReceiver : specialReceivers.get(event)) {
                eventReceiver.receiveEvent(null);
            }
            return true;
        }*/
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("lastSuccessCount", lastSuccessCount);
        compound.setInteger(lastResultType.getTypeName(), lastResultAmount);
        compound.setString("pack", packId);
        compound.setString("block", reduxBlock.getId());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.lastSuccessCount = compound.getInteger("lastSuccessCount");
        this.packId = compound.getString("pack");
        setupTileEntity(compound.getString("block"));
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.pos, 0, nbttagcompound);
    }

    /*protected class ReduxBlockEventReceiver implements ICommandSender {
        private final Trigger triggerScript;
        protected Event lastEvent = null;

        public ReduxBlockEventReceiver(Trigger triggerScript) {
            this.triggerScript = triggerScript;
        }

        public Trigger getTriggerScript() {
            return triggerScript;
        }

        public Event getLastEvent() {
            return lastEvent;
        }

        @Override
        public String getName() {
            return reduxBlock.getId();
        }

        @Override
        public IChatComponent getDisplayName() {
            return new ChatComponentText(reduxBlock.getName());
        }

        @Override
        public void addChatMessage(IChatComponent message) {
            reduxVariables.put("msg", message.getUnformattedTextForChat());
        }

        @Override
        public boolean canUseCommand(int permLevel, String commandName) {
            return permLevel <= 2;
        }

        @Override
        public BlockPos getPosition() {
            return ReduxCommandBlockTileEntity.this.getPos();
        }

        @Override
        public Vec3 getPositionVector() {
            return new Vec3((double)ReduxCommandBlockTileEntity.this.pos.getX() + 0.5D, (double)ReduxCommandBlockTileEntity.this.pos.getY() + 0.5D, (double)ReduxCommandBlockTileEntity.this.pos.getZ() + 0.5D);
        }

        @Override
        public World getEntityWorld() {
            return ReduxCommandBlockTileEntity.this.getWorld();
        }

        @Override
        public Entity getCommandSenderEntity() {
            return null;
        }

        @Override
        public boolean sendCommandFeedback() {
            return false;
        }

        @Override
        public void setCommandStat(CommandResultStats.Type type, int amount) {
            ReduxCommandBlockTileEntity.this.lastResultType = type;
            ReduxCommandBlockTileEntity.this.lastResultAmount = amount;
            if (type == CommandResultStats.Type.SUCCESS_COUNT) {
                commandResultStack.push(amount);
                IBlockState defaultState = ReduxCommandBlockTileEntity.this.getWorld().getBlockState(ReduxCommandBlockTileEntity.this.pos).getBlock().getDefaultState();
                ReduxCommandBlockTileEntity.this.getWorld().setBlockState(ReduxCommandBlockTileEntity.this.pos, defaultState.withProperty(ReduxBlock.SUCCESS_COUNT_META, amount));
                ReduxCommandBlockTileEntity.this.lastSuccessCount = amount;
            }
        }

        public void setLastTest(int result) {
            reduxVariables.put("test", String.valueOf(result));
        }

        public void receiveEvent(Event event) {
            if (worldObj.isRemote)
                return;
            commandResultStack.clear();

            BlockPos blockPos = ReduxCommandBlockTileEntity.this.pos;
            // Check if the block has changed as result of an event. I.E. BlockBreak
            if (ReduxCommandBlockTileEntity.this.getWorld().getBlockState(pos).getBlock().getClass() != ReduxBlock.class) {
                return;
            }

            EntityPlayer playerTrigger = null;
            if (event != null) {
                if (event instanceof ChunkWatchEvent) {
                    ChunkWatchEvent chunkWatchEvent = (ChunkWatchEvent) event;
                    reduxVariables.put("chunk_x", String.valueOf(chunkWatchEvent.chunk.chunkXPos));
                    reduxVariables.put("chunk_z", String.valueOf(chunkWatchEvent.chunk.chunkZPos));
                    playerTrigger = chunkWatchEvent.player;
                } else if (event instanceof BlockEvent) {
                    BlockEvent blockEvent = (BlockEvent) event;
                    if (triggerScript.blockHasToBeTheCause() && !blockEvent.pos.equals(ReduxCommandBlockTileEntity.this.pos)) {
                        return;
                    }
                    reduxVariables.put("target_x", String.valueOf(blockEvent.pos.getX()));
                    reduxVariables.put("target_y", String.valueOf(blockEvent.pos.getY()));
                    reduxVariables.put("target_z", String.valueOf(blockEvent.pos.getZ()));
                    reduxVariables.put("chunk_x", String.valueOf(blockEvent.world.getChunkFromBlockCoords(blockEvent.pos).getChunkCoordIntPair().chunkXPos));
                    reduxVariables.put("chunk_z", String.valueOf(blockEvent.world.getChunkFromBlockCoords(blockEvent.pos).getChunkCoordIntPair().chunkZPos));
                    reduxVariables.put("target_id", ((BlockEvent) event).state.getBlock().getUnlocalizedName());

                    switch(triggerScript.getTriggerEvent()) {
                        case BlockBreakEvent:
                            playerTrigger = ((BlockEvent.BreakEvent)event).getPlayer();
                            break;
                        case BlockPlaceEvent:
                        case  BlockMultiPlaceEvent:
                            playerTrigger = ((BlockEvent.PlaceEvent)event).player;
                            break;
                        case BlockHarvestDropsEvent:
                            playerTrigger = ((BlockEvent.HarvestDropsEvent)event).harvester;
                            break;
                    }
                } else if (event instanceof ServerChatEvent) {
                    ServerChatEvent chatEvent = (ServerChatEvent) event;
                    reduxVariables.put("chat_message", chatEvent.message);
                    playerTrigger = chatEvent.player;
                }  else if (event instanceof ExplosionEvent) {
                    ExplosionEvent explosionEvent = (ExplosionEvent) event;
                    reduxVariables.put("target_x", String.valueOf((int)explosionEvent.explosion.getPosition().xCoord));
                    reduxVariables.put("target_y", String.valueOf((int)explosionEvent.explosion.getPosition().yCoord));
                    reduxVariables.put("target_z", String.valueOf((int)explosionEvent.explosion.getPosition().yCoord));

                    reduxVariables.put("chunk_x", String.valueOf(explosionEvent.world.getChunkFromBlockCoords(new BlockPos(explosionEvent.explosion.getPosition())).getChunkCoordIntPair().chunkXPos));
                    reduxVariables.put("chunk_z", String.valueOf(explosionEvent.world.getChunkFromBlockCoords(new BlockPos(explosionEvent.explosion.getPosition())).getChunkCoordIntPair().chunkZPos));
                }
                reduxVariables.put("event_name", triggerScript.getTriggerEvent().name());
            } else if (event == null && lastEventArgs != null && lastEventArgs.length > 0) {
                if (triggerScript.getTriggerEvent() == Trigger.TriggerEvent.OnEntityCollide) {
                    Entity entity = (Entity) lastEventArgs[0];
                    reduxVariables.put("entity_name", entity.getName());
                } else if (triggerScript.getTriggerEvent() == Trigger.TriggerEvent.OnInteract) {
                    boolean rightClicked = (Boolean)lastEventArgs[0];
                    playerTrigger = (EntityPlayerMP) lastEventArgs[1];
                    reduxVariables.put("mouse_button", rightClicked ? "right" : "left");
                    if (rightClicked) {
                        EnumFacing sideClicked = (EnumFacing) lastEventArgs[2];
                        reduxVariables.put("side_clicked", sideClicked.getName2());
                        reduxVariables.put("hit_x", String.valueOf(lastEventArgs[3]));
                        reduxVariables.put("hit_y", String.valueOf(lastEventArgs[4]));
                        reduxVariables.put("hit_z", String.valueOf(lastEventArgs[5]));
                    }
                } else if (triggerScript.getTriggerEvent() == Trigger.TriggerEvent.OnRestoneStrengthChange) {
                    for (int i = 0; i < 7; i++) {
                        if (i == 7) {
                            reduxVariables.put("weak", String.valueOf(i));
                        } else {
                            reduxVariables.put("strong:" + EnumFacing.values()[i].getName2(), String.valueOf(i));
                        }
                    }
                }
            }
            if (playerTrigger != null) {
                reduxVariables.put("player", playerTrigger.getName());
                reduxVariables.put("player_x", String.valueOf(playerTrigger.getPosition().getX()));
                reduxVariables.put("player_y", String.valueOf(playerTrigger.getPosition().getY()));
                reduxVariables.put("player_z", String.valueOf(playerTrigger.getPosition().getZ()));
                reduxVariables.put("active_slot", String.valueOf(playerTrigger.inventory.currentItem));
                if (playerTrigger.inventory.getCurrentItem() != null)
                    reduxVariables.put("active_item", playerTrigger.inventory.getCurrentItem().getItem().delegate.name());
                else
                    reduxVariables.put("active_item", "minecraft:hand");
            }
            reduxVariables.put("world_id", String.valueOf(ReduxCommandBlockTileEntity.this.worldObj.provider.getDimensionId()));
            reduxVariables.put("world_name", ReduxCommandBlockTileEntity.this.worldObj.getWorldInfo().getWorldName());
            reduxVariables.put("x", String.valueOf(blockPos.getX()));
            reduxVariables.put("y", String.valueOf(blockPos.getY()));
            reduxVariables.put("z", String.valueOf(blockPos.getZ()));

            ICommandManager icommandmanager = FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
            lastEvent = event;
            int skipCount = 0;
            for (String s : triggerScript.getCommands()) {
                if (skipCount > 0) {
                    skipCount--;
                    continue;
                }

                String parsedCommand = s;
                Pattern reduxPattern = Pattern.compile("\\$redux\\[\\w{1,}\\]");
                Matcher reduxMatcher = reduxPattern.matcher(parsedCommand);
                Pattern commandPattern = Pattern.compile("\\[\\w{1,}\\]");
                while (reduxMatcher.find()) {
                    MatchResult result = reduxMatcher.toMatchResult();
                    Matcher commandMatcher = commandPattern.matcher(result.group());
                    if (!commandMatcher.find())
                        continue;
                    String command = commandMatcher.toMatchResult().group().replaceFirst("\\[", "").replaceFirst("\\]", "");

                    if (command.equalsIgnoreCase("PEEK") || command.equalsIgnoreCase("POP")) {
                        if (command.equalsIgnoreCase("PEEK")) {
                            reduxMatcher = reduxPattern.matcher(parsedCommand = reduxMatcher.replaceFirst(String.valueOf(commandResultStack.peek())));
                        } else if (command.equalsIgnoreCase("POP")) {
                            reduxMatcher = reduxPattern.matcher(parsedCommand = reduxMatcher.replaceFirst(String.valueOf(commandResultStack.pop())));
                        }
                    } else if (reduxVariables.containsKey(command)) {
                        reduxMatcher = reduxPattern.matcher(parsedCommand = reduxMatcher.replaceFirst(reduxVariables.get(command)));
                    }
                }

                if (parsedCommand.startsWith("/stopscript")) {
                    String[] split = parsedCommand.split(" ");
                    if (split.length == 2) {
                        boolean stopScript;
                        if (isNumeric(split[1])) {
                            stopScript = Integer.parseInt(split[1]) != 0;
                        } else {
                            stopScript = split[1].equalsIgnoreCase("true");
                        }
                        if (stopScript)
                            break;
                    }
                    continue;
                } else if (parsedCommand.startsWith("/stopdefault")) {
                    String[] split = parsedCommand.split(" ");
                    if (split.length == 2) {
                        boolean stopEvent;
                        if (isNumeric(split[1])) {
                            stopEvent = Integer.parseInt(split[1]) != 0;
                        } else {
                            stopEvent = split[1].equalsIgnoreCase("true");
                        }
                        if (event != null && event.isCancelable())
                            event.setCanceled(stopEvent);
                    }
                    continue;
                } else if (parsedCommand.startsWith("/skip")) {
                    String[] split = parsedCommand.split(" ");
                    if (split.length == 3) {
                        try {
                            int skip = Integer.parseInt(split[1]);
                            boolean test;
                            if (isNumeric(split[2])) {
                                test = Integer.parseInt(split[2]) != 0;
                            } else {
                                test = split[2].equalsIgnoreCase("true");
                            }
                            if (test && skip > 0)
                                skipCount = skip;
                        } catch (NumberFormatException ignored) {}
                    }
                    continue;
                }

                icommandmanager.executeCommand(this, parsedCommand);
            }
        }
    }*/

    protected static boolean isNumeric(String str) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(str, pos);
        return str.length() == pos.getIndex();
    }
}
