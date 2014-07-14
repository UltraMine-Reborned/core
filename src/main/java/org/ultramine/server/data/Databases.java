package org.ultramine.server.data;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.ultramine.server.ConfigurationHandler;
import org.ultramine.server.UltramineServerConfig;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public class Databases
{
	private static Map<String, DataSource> databases = new HashMap<String, DataSource>();
	
	public static void init()
	{
		for(Map.Entry<String, UltramineServerConfig.Database> ent : ConfigurationHandler.getServerConfig().databases.entrySet())
		{
			UltramineServerConfig.Database info = ent.getValue();
			
			BasicDataSource ds = new BasicDataSource();
			if(info.url.startsWith("jdbc:mysql:"))
				ds.setDriverClassName("com.mysql.jdbc.Driver");
			ds.setUrl(info.url);
			ds.setUsername(info.username);
			ds.setPassword(info.password);
			
			ds.setMaxActive(info.maxConnections);
			
			databases.put(ent.getKey(), ds);
		}
	}
	
	public static DataSource getDataSource(String name)
	{
		DataSource ds = databases.get(name);
		if(ds == null)
			throw new RuntimeException("DataSource for name: " + name + " not found! Check your server.yml");
		return ds;
	}
}
