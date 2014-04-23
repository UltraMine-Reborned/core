package net.minecraft.client.multiplayer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class ServerList
{
	private static final Logger logger = LogManager.getLogger();
	private final Minecraft mc;
	private final List servers = new ArrayList();
	private static final String __OBFID = "CL_00000891";

	public ServerList(Minecraft par1Minecraft)
	{
		this.mc = par1Minecraft;
		this.loadServerList();
	}

	public void loadServerList()
	{
		try
		{
			this.servers.clear();
			NBTTagCompound nbttagcompound = CompressedStreamTools.read(new File(this.mc.mcDataDir, "servers.dat"));

			if (nbttagcompound == null)
			{
				return;
			}

			NBTTagList nbttaglist = nbttagcompound.getTagList("servers", 10);

			for (int i = 0; i < nbttaglist.tagCount(); ++i)
			{
				this.servers.add(ServerData.getServerDataFromNBTCompound(nbttaglist.getCompoundTagAt(i)));
			}
		}
		catch (Exception exception)
		{
			logger.error("Couldn\'t load server list", exception);
		}
	}

	public void saveServerList()
	{
		try
		{
			NBTTagList nbttaglist = new NBTTagList();
			Iterator iterator = this.servers.iterator();

			while (iterator.hasNext())
			{
				ServerData serverdata = (ServerData)iterator.next();
				nbttaglist.appendTag(serverdata.getNBTCompound());
			}

			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setTag("servers", nbttaglist);
			CompressedStreamTools.safeWrite(nbttagcompound, new File(this.mc.mcDataDir, "servers.dat"));
		}
		catch (Exception exception)
		{
			logger.error("Couldn\'t save server list", exception);
		}
	}

	public ServerData getServerData(int par1)
	{
		return (ServerData)this.servers.get(par1);
	}

	public void removeServerData(int par1)
	{
		this.servers.remove(par1);
	}

	public void addServerData(ServerData par1ServerData)
	{
		this.servers.add(par1ServerData);
	}

	public int countServers()
	{
		return this.servers.size();
	}

	public void swapServers(int par1, int par2)
	{
		ServerData serverdata = this.getServerData(par1);
		this.servers.set(par1, this.getServerData(par2));
		this.servers.set(par2, serverdata);
		this.saveServerList();
	}

	public void func_147413_a(int p_147413_1_, ServerData p_147413_2_)
	{
		this.servers.set(p_147413_1_, p_147413_2_);
	}

	public static void func_147414_b(ServerData p_147414_0_)
	{
		ServerList serverlist = new ServerList(Minecraft.getMinecraft());
		serverlist.loadServerList();

		for (int i = 0; i < serverlist.countServers(); ++i)
		{
			ServerData serverdata1 = serverlist.getServerData(i);

			if (serverdata1.serverName.equals(p_147414_0_.serverName) && serverdata1.serverIP.equals(p_147414_0_.serverIP))
			{
				serverlist.func_147413_a(i, p_147414_0_);
				break;
			}
		}

		serverlist.saveServerList();
	}
}