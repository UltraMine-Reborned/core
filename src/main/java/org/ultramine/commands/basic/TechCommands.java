package org.ultramine.commands.basic;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import static net.minecraft.util.EnumChatFormatting.*;
import net.minecraft.world.WorldServer;

import org.ultramine.commands.Command;
import org.ultramine.commands.CommandContext;

public class TechCommands
{
	@Command(
			name = "id",
			group = "technical",
			permissions = {"command.id"},
			syntax = {"<%id>"}
	)
	public static void id(CommandContext ctx)
	{
		Item item = ctx.get("id").asItem();
		ctx.sendMessage("ID: %s", Item.itemRegistry.getNameForObject(item));
		ctx.sendMessage("Internal ID: %s", Item.itemRegistry.getIDForObject(item));
		ctx.sendMessage("Unlocalized name: %s", new ChatComponentText(item.getUnlocalizedName()));
		ctx.sendMessage("Localized name: %s", new ChatComponentTranslation(item.getUnlocalizedName()+".name"));
		ctx.sendMessage("Is block: %s", item instanceof ItemBlock);
		ctx.sendMessage("Class name: %s", item.getClass().getName());
	}
	
	@Command(
			name = "uptime",
			aliases = {"ticks", "lagometer"},
			group = "technical",
			permissions = {"command.uptime"}
	)
	public static void uptime(CommandContext ctx)
	{
		double tps = Math.round(MinecraftServer.getServer().currentTPS*10)/10d;
		double downtime = MinecraftServer.getServer().currentWait/1000/1000d;
		int load = (int)Math.round((50-downtime)/50*100);
		int uptime = (int)((System.currentTimeMillis() - MinecraftServer.getServer().startTime)/1000);
		ctx.sendMessage(DARK_GREEN, "command.uptime.msg.up", String.format("%dd %dh %dm %ds", uptime/(60*60*24), uptime/(60*60)%24, uptime/60%60, uptime%60));
		ctx.sendMessage(load > 100 ? RED : DARK_GREEN, "command.uptime.msg.load", Integer.toString(load).concat("%"));
		ctx.sendMessage(tps < 15 ? RED : DARK_GREEN, "command.uptime.msg.tps",  Double.toString(tps),
				Integer.toString((int)(tps/20*100)).concat("%"));
	}
	
	@Command(
			name = "debuginfo",
			group = "technical",
			permissions = {"command.debuginfo"},
			syntax = {
					"",
					"<world>",
					"<player>"
			}
	)
	public static void debuginfo(CommandContext ctx)
	{
		if(ctx.contains("world") || ctx.getArgs().length == 0)
		{
			WorldServer world = ctx.contains("world") ? ctx.get("world").asWorld() : ctx.getSenderAsPlayer().getServerForPlayer();
			ctx.sendMessage("World: %s, Dimension: %s", world.getWorldInfo().getWorldName(), world.provider.dimensionId);
			ctx.sendMessage("Chunks loaded:  %s", world.theChunkProviderServer.getLoadedChunkCount());
			ctx.sendMessage("Chunks active:  %s", world.getActiveChunkSetSize());
			ctx.sendMessage("Chunks for unload:  %s", world.theChunkProviderServer.chunksToUnload.size());
			ctx.sendMessage("Players: %s", world.playerEntities.size());
			ctx.sendMessage("Entities:  %s", world.loadedEntityList.size());
			ctx.sendMessage("TileEntities:  %s", world.loadedTileEntityList.size());
		}
		else
		{
			EntityPlayerMP player = ctx.get("player").asPlayer();
			ctx.sendMessage("Username: %s", player.getCommandSenderName());
			ctx.sendMessage("UUID: %s", player.getGameProfile().getId());
			ctx.sendMessage("Chunk send rate: %s", player.getChunkMgr().getRate());
			ctx.sendMessage("View distance: %s", player.getRenderDistance());
		}
	}
	
	@Command(
			name = "memstat",
			group = "technical",
			permissions = {"command.memstat"}
	)
	public static void memstat(CommandContext ctx)
	{
		ctx.sendMessage("Heap max: %sm", Runtime.getRuntime().maxMemory() >> 20);
		ctx.sendMessage("Heap total: %sm", Runtime.getRuntime().totalMemory() >> 20);
		ctx.sendMessage("Heap free: %sm", Runtime.getRuntime().freeMemory() >> 20);
	}
}
