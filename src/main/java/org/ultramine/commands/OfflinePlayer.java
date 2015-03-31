package org.ultramine.commands;

import org.ultramine.server.data.player.PlayerData;
import org.ultramine.server.util.BasicTypeFormatter;

import com.google.common.base.Function;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.FakePlayer;

public class OfflinePlayer
{
	private final ServerConfigurationManager mgr;
	private final PlayerData data;
	
	public OfflinePlayer(MinecraftServer server, PlayerData profile)
	{
		this.mgr = server.getConfigurationManager();
		this.data = profile;
	}
	
	public PlayerData getPlayerData()
	{
		return data;
	}
	
	public EntityPlayerMP getIfOnline()
	{
		return mgr.getPlayerByUsername(data.getProfile().getName());
	}
	
	public void loadPlayer(Function<EntityPlayerMP, Void> callback)
	{
		EntityPlayerMP exists = getIfOnline();
		if(exists != null)
			callback.apply(exists);
		else
			mgr.getDataLoader().loadOffline(data.getProfile(), callback);
			
	}
	
	//Totally unsafe...
	public void saveFakePlayer(EntityPlayerMP player)
	{
		if(!(player instanceof FakePlayer))
			return;
		if(!player.getGameProfile().equals(data.getProfile()))
			throw new RuntimeException("Player not this! this:"+data.getProfile().getName()+" other:"+player.getGameProfile().getName());
		if(getIfOnline() != null)
			throw new RuntimeException("Can't save FakePlayer: real player is online! username:"+data.getProfile().getName());
		
		mgr.getDataLoader().saveOfflinePlayer((FakePlayer)player);
	}
	
	public void sendMessage(EnumChatFormatting tplColor, EnumChatFormatting argsColor, String msg, Object... args)
	{
		EntityPlayerMP player = getIfOnline();
		if(player != null)
			player.addChatMessage(BasicTypeFormatter.formatMessage(tplColor, argsColor, msg, args));
	}
	
	public void sendMessage(EnumChatFormatting argsColor, String msg, Object... args)
	{
		sendMessage(EnumChatFormatting.GOLD, argsColor, msg, args);
	}
	
	public void sendMessage(String msg, Object... args)
	{
		sendMessage(EnumChatFormatting.YELLOW, msg, args);
	}
}
