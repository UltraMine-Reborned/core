package net.minecraft.network.rcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public interface IServer
{
	int getIntProperty(String var1, int var2);

	String getStringProperty(String var1, String var2);

	void setProperty(String var1, Object var2);

	void saveProperties();

	String getSettingsFilename();

	String getHostname();

	int getPort();

	String getMotd();

	String getMinecraftVersion();

	int getCurrentPlayerCount();

	int getMaxPlayers();

	String[] getAllUsernames();

	String getFolderName();

	String getPlugins();

	String handleRConCommand(String var1);

	boolean isDebuggingEnabled();

	void logInfo(String var1);

	void logWarning(String var1);

	void logSevere(String var1);

	void logDebug(String var1);
}