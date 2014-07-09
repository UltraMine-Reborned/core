package org.ultramine.server.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.ultramine.commands.basic.FastWarpCommand;
import org.ultramine.server.ConfigurationHandler;
import org.ultramine.server.data.player.PlayerData;
import org.ultramine.server.data.player.PlayerDataExtension;
import org.ultramine.server.data.player.PlayerDataExtensionInfo;
import org.ultramine.server.data.player.io.PlayerDataIOExecutor;
import org.ultramine.server.util.WarpLocation;

import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.command.CommandHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.event.ForgeEventFactory;

public class ServerDataLoader
{
	private static final boolean isClient = FMLCommonHandler.instance().getSide().isClient();
	private final ServerConfigurationManager mgr;
	private final IDataProvider dataProvider;
	private final List<PlayerDataExtensionInfo> dataExtinfos = new ArrayList<PlayerDataExtensionInfo>();
	private final Map<UUID, PlayerData> playerDataCache = new HashMap<UUID, PlayerData>();
	private final Map<String, WarpLocation> warps = new HashMap<String, WarpLocation>();
	private final List<String> fastWarps = new ArrayList<String>();
	
	public ServerDataLoader(ServerConfigurationManager mgr)
	{
		this.mgr = mgr;
		dataProvider = new NBTFileDataProvider(mgr);
	}
	
	public IDataProvider getDataProvider()
	{
		return dataProvider;
	}
	
	public PlayerData getPlayerData(GameProfile profile)
	{
		return playerDataCache.get(profile.getId());
	}
	
	public PlayerData getPlayerData(UUID id)
	{
		return playerDataCache.get(id);
	}
	
	public PlayerData getPlayerData(String username)
	{
		return playerDataCache.get(mgr.getServerInstance().func_152358_ax().func_152655_a(username));
	}
	
	public WarpLocation getWarp(String name)
	{
		return warps.get(name);
	}
	
	public void setWarp(String name, WarpLocation warp)
	{
		warps.put(name, warp);
		dataProvider.saveWarp(name, warp);
	}
	
	public boolean removeWarp(String name)
	{
		if(warps.remove(name) != null)
		{
			dataProvider.removeWarp(name);
			return true;
		}
		return false;
	}
	
	public Map<String, WarpLocation> getWarps()
	{
		return warps;
	}
	
	public void addFastWarp(String name)
	{
		if(fastWarps.add(name))
		{
			dataProvider.saveFastWarp(name);
			((CommandHandler)mgr.getServerInstance().getCommandManager()).getRegistry().registerCommand(new FastWarpCommand(name));
		}
	}
	
	public boolean removeFastWarp(String name)
	{
		if(fastWarps.remove(name))
		{
			dataProvider.removeFastWarp(name);
			((CommandHandler)mgr.getServerInstance().getCommandManager()).getRegistry().getCommandMap().remove(name);
			return true;
		}
		return false;
	}
	
	public List<String> getFastWarps()
	{
		return fastWarps;
	}
	
	public void loadCache()
	{
		for(PlayerData data : dataProvider.loadAllPlayerData())
			playerDataCache.put(data.getProfile().getId(), data);
		warps.putAll(dataProvider.loadWarps());
		fastWarps.addAll(dataProvider.loadFastWarps());
	}
	
	public void addDefaultWarps()
	{
		if(!warps.containsKey("spawn"))
		{
			WorldInfo wi = mgr.getServerInstance().getMultiWorld().getWorldByID(0).getWorldInfo();
			setWarp("spawn", new WarpLocation(0, wi.getSpawnX(), wi.getSpawnY(), wi.getSpawnZ(), 0, 0, 20));
		}
		if(!fastWarps.contains("spawn"))
		{
			fastWarps.add("spawn");
			dataProvider.saveFastWarp("spawn");
		}
		if(!isClient)
		{
			String firstSpawn = ConfigurationHandler.getServerConfig().spawnLocations.firstSpawn;
			String deathSpawn = ConfigurationHandler.getServerConfig().spawnLocations.deathSpawn;
			if(!warps.containsKey(firstSpawn)) setWarp(firstSpawn, getWarp("spawn"));
			if(!warps.containsKey(deathSpawn)) setWarp(deathSpawn, getWarp("spawn"));
		}
	}
	
	public void initializeConnectionToPlayer(NetworkManager network, EntityPlayerMP player, NetHandlerPlayServer nethandler)
	{
		if(isClient)
		{
			NBTTagCompound nbt = mgr.readPlayerDataFromFile(player);
			player.setData(getDataProvider().loadPlayerData(player.getGameProfile()));
			mgr.initializeConnectionToPlayer_body(network, player, nethandler, nbt);
		}
		else
		{
			PlayerDataIOExecutor.requestData(getDataProvider(), network, player, nethandler, this, !playerDataCache.containsKey(player.getGameProfile().getId()));
		}
	}
	
	public void playerLoadCallback(NetworkManager network, EntityPlayerMP player, NetHandlerPlayServer nethandler, NBTTagCompound nbt, PlayerData data)
	{
		if(data != null)
		{
			player.setData(data);
			playerDataCache.put(data.getProfile().getId(), data);
		}
		else
		{
			player.setData(playerDataCache.get(player.getGameProfile().getId()));
		}
		if(nbt == null) //first login
		{
			WarpLocation spawnWarp = getWarp(isClient ? "spawn" : ConfigurationHandler.getServerConfig().spawnLocations.firstSpawn);
			WarpLocation spawn = (spawnWarp != null ? spawnWarp : getWarp("spawn")).randomize();
			player.setLocationAndAngles(spawn.x, spawn.y, spawn.z, spawn.yaw, spawn.pitch);
		}
		ForgeEventFactory.firePlayerLoadingEvent(player, ((SaveHandler)mgr.getPlayerNBTLoader()).getPlayerSaveDir(), player.getUniqueID().toString());
		mgr.initializeConnectionToPlayer_body(network, player, nethandler, nbt);
	}
	
	public void savePlayer(EntityPlayerMP player)
	{
		ForgeEventFactory.firePlayerSavingEvent(player, ((SaveHandler)mgr.getPlayerNBTLoader()).getPlayerSaveDir(), player.getUniqueID().toString());
		NBTTagCompound nbt = new NBTTagCompound();
		player.writeToNBT(nbt);
		
		getDataProvider().savePlayer(player.getGameProfile(), nbt);
		getDataProvider().savePlayerData(player.getData());
	}
	
	public void registerPlayerDataExt(Class<? extends PlayerDataExtension> clazz, String nbtTagName)
	{
		dataExtinfos.add(new PlayerDataExtensionInfo(clazz, nbtTagName));
	}
	
	public List<PlayerDataExtensionInfo> getDataExtProviders()
	{
		return dataExtinfos;
	}
}
