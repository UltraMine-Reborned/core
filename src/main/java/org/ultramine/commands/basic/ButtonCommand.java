package org.ultramine.commands.basic;

import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.ultramine.commands.Command;
import org.ultramine.commands.CommandContext;
import org.ultramine.server.ConfigurationHandler;
import org.ultramine.server.chunk.ChunkHash;
import org.ultramine.server.event.SetBlockEvent;
import org.ultramine.server.util.MinecraftUtil;
import org.ultramine.server.util.YamlConfigProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.SERVER)
public class ButtonCommand
{
    private static ButtonCommand instance;

    private final MinecraftServer server;
    private final File storage;

    private ButtonCMDs buttons;
    private final TIntObjectMap<TLongObjectHashMap<ButtonCMDs.ButtonCMD>> buttonMap = new TIntObjectHashMap<>();

    public ButtonCommand(MinecraftServer server)
    {
        this.server = server;
        this.storage = new File(ConfigurationHandler.getStorageDir(), "buttonCommand.yml");
        instance = this;
    }

    public void load(FMLServerStartingEvent e)
    {
        e.registerCommands(this.getClass());
        MinecraftForge.EVENT_BUS.register(this);
        buttons = YamlConfigProvider.getOrCreateConfig(storage, ButtonCMDs.class);
        for(ButtonCMDs.ButtonCMD cmd : buttons.buttons)
            putCmd(cmd);
    }

    public void unload()
    {
        MinecraftForge.EVENT_BUS.unregister(this);
        instance = null;
    }

    private void save()
    {
        YamlConfigProvider.saveConfig(storage, buttons);
    }

    private void putCmd(ButtonCMDs.ButtonCMD bt)
    {
        TLongObjectHashMap<ButtonCMDs.ButtonCMD> map = buttonMap.get(bt.dim);
        if(map == null)
        {
            map = new TLongObjectHashMap<>();
            buttonMap.put(bt.dim, map);
        }

        map.put(bt.getKey(), bt);
    }

    private void add(ButtonCMDs.ButtonCMD bt)
    {
        buttons.buttons.add(bt);
        putCmd(bt);
        save();
    }

    private boolean exists(int dim, int x, int y, int z)
    {
        TLongObjectHashMap<ButtonCMDs.ButtonCMD> map = buttonMap.get(dim);
        return map != null && map.contains(ChunkHash.blockCoordToHash(x, y, z));
    }

    private void add(int dim, int x, int y, int z, String permission, String[] cmds)
    {
        for(int i = 0; i < cmds.length; i++)
            cmds[i] = cmds[i].trim();
        ButtonCMDs.ButtonCMD bt = new ButtonCMDs.ButtonCMD(dim, x, y, z, permission, cmds);
        add(bt);
    }

    private ButtonCMDs.ButtonCMD get(int dim, int x, int y, int z)
    {
        TLongObjectHashMap<ButtonCMDs.ButtonCMD> map = buttonMap.get(dim);
        return map == null ? null : map.get(ChunkHash.blockCoordToHash(x, y, z));
    }

    private void remove(int dim, int x, int y, int z)
    {
        TLongObjectHashMap<ButtonCMDs.ButtonCMD> map = buttonMap.get(dim);
        if(map != null)
        {
            ButtonCMDs.ButtonCMD bt = map.remove(ChunkHash.blockCoordToHash(x, y, z));
            if(bt != null)
            {
                buttons.buttons.remove(bt);
                save();
            }
        }
    }

    @Command(
            name = "buttoncommand",
            aliases = {"bc"},
            group = "basic",
            permissions = {"command.buttoncommand"},
            syntax = {
                    "[create c] <commands>...",
                    "[perm p] <perm> <commands>...",
                    "[get remove r rm]"
            }
    )
    public static void buttonCommand(CommandContext ctx)
    {
        EntityPlayerMP player = ctx.getSenderAsPlayer();
        MovingObjectPosition obj = MinecraftUtil.getMovingObjectPosition(player);
        ctx.check(obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK, "command.buttoncommand.fail.noblock");
        if(ctx.getAction().startsWith("c") || ctx.getAction().startsWith("p"))
        {
            ctx.check(!instance.exists(player.dimension, obj.blockX, obj.blockY, obj.blockZ), "command.buttoncommand.fail.already");
            instance.add(player.dimension, obj.blockX, obj.blockY, obj.blockZ, ctx.contains("perm") ? ctx.get("perm").asString() : null,
                    ctx.get("commands").asString().split(";"));
            ctx.sendMessage("command.buttoncommand.success.create");
        }
        else if(ctx.getAction().startsWith("r"))
        {
            ctx.check(instance.exists(player.dimension, obj.blockX, obj.blockY, obj.blockZ), "command.buttoncommand.fail.none");
            instance.remove(player.dimension, obj.blockX, obj.blockY, obj.blockZ);
            ctx.sendMessage("command.buttoncommand.success.remove");

        }
        else if(ctx.getAction().startsWith("g"))
        {
            ButtonCMDs.ButtonCMD bt = instance.get(player.dimension, obj.blockX, obj.blockY, obj.blockZ);
            ctx.check(bt != null, "command.buttoncommand.fail.none");
            ctx.sendMessage("command.buttoncommand.get.head");
            //noinspection ConstantConditions
            for(String cmd : bt.commands)
                ctx.sendMessage("    - %s", cmd);
        }
    }

    @SubscribeEvent
    public void onPlayerInteractEvent(PlayerInteractEvent e)
    {
        if(e.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
            return;
        ButtonCMDs.ButtonCMD bt = get(e.world.provider.dimensionId, e.x, e.y, e.z);
        if(bt != null)
        {
            e.setCanceled(true);
            if(bt.permission == null || ((EntityPlayerMP) e.entityPlayer).hasPermission(bt.permission))
            {
                for(String cmd : bt.commands)
                    server.getCommandManager().executeCommand(server, cmd.replace("@p", e.entityPlayer.getGameProfile().getName()));
            }
            else
            {
                e.entityPlayer.addChatMessage(new ChatComponentTranslation("command.buttoncommand.noperms").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onBreakEvent(SetBlockEvent e)
    {
        remove(e.world.provider.dimensionId, e.x, e.y, e.z);
    }

    public static class ButtonCMDs
    {
        public List<ButtonCMD> buttons = new ArrayList<>();

        public static class ButtonCMD
        {
            public int dim;
            public int x;
            public int y;
            public int z;
            public String permission;
            public String[] commands;

            public ButtonCMD(){}
            public ButtonCMD(int dim, int x, int y, int z, String permission, String[] commands)
            {
                this.dim = dim;
                this.x = x;
                this.y = y;
                this.z = z;
                this.permission = permission;
                this.commands = commands;
            }

            public boolean equals(Object o)
            {
                if(!(o instanceof ButtonCMD))
                    return false;
                ButtonCMD cmd = (ButtonCMD)o;
                return dim == cmd.dim && x == cmd.x && y == cmd.y && z == cmd.z;
            }

            public int hashCode()
            {
                return dim ^ ChunkHash.chunkCoordToHash(x, y, z);
            }

            public long getKey()
            {
                return ChunkHash.blockCoordToHash(x, y, z);
            }
        }
    }
}

