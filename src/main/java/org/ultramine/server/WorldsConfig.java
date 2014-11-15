package org.ultramine.server;

import java.util.HashMap;
import java.util.Map;

public class WorldsConfig
{
	public WorldConfig global = new WorldConfig();
	public Map<String, WorldConfig> worlds = new HashMap<String, WorldConfig>();
	
	public static class WorldConfig
	{
		public int dimension;
		public Generation generation;
		public MobSpawn mobSpawn;
		public Settings settings;
		public Border[] borders;
		public ChunkLoading chunkLoading;
		public LoadBalancer loadBalancer;
		
		public static class Generation
		{
			public String seed;
			public int providerID = 0;
			public String levelType = "DEFAULT";
			public String generatorSettings = "";
			public boolean generateStructures = true;
		}
		
		public static class MobSpawn
		{
			public boolean allowAnimals = true;
			public boolean spawnAnimals = true;
			public boolean spawnMonsters = true;
			public boolean allowNPCs = true;
			public MobSpawnEngine spawnEngine = MobSpawnEngine.OLD;
			public NewEngineSettings newEngineSettings;
			
			public static enum MobSpawnEngine
			{
				OLD, NEW, NONE
			}
			
			public static class NewEngineSettings
			{
				public MonsterSettings monsters;
				public PerTypeMobSpawnSettings animals;
				public PerTypeMobSpawnSettings water;
				public PerTypeMobSpawnSettings ambient;
				
				public static class PerTypeMobSpawnSettings
				{
					public boolean enabled;
					public int minRadius;
					public int maxRadius;
					public int minPlayerDistance;
					public int performInterval;
					public int localCheckRadius;
					public int localLimit;
				}
				
				public static class MonsterSettings extends PerTypeMobSpawnSettings
				{
					public int nightlyLocalLimit;
				}
			}
		}
		
		public static class Settings
		{
			public String difficulty = "1";
			public boolean pvp = true;
			public int maxBuildHeight = 256;
			public WorldTime time = WorldTime.NORMAL;
			public Weather weather = Weather.NORMAL;
			public boolean useIsolatedPlayerData = false;
			public String respawnOnWarp = null;
			public String reconnectOnWarp = null;
			
			public enum WorldTime
			{
				NORMAL, DAY, NIGHT, FIXED
			}
			
			public enum Weather
			{
				NORMAL, NONE, RAIN, THUNDER
			}
		}
		
		public static class Border
		{
			public int x;
			public int z;
			public int radius;
			public boolean round;
		}
		
		public static class ChunkLoading
		{
			public int viewDistance  = 10;
			public int chunkUpdateRadius = 7;
			public int chunkCacheSize;
			public boolean enableChunkLoaders = true;
		}
		
		public static class LoadBalancer
		{
			public Limits limits;
			public static class Limits
			{
				public PerChunkEntityLimits monsters;
				public PerChunkEntityLimits animals;
				public PerChunkEntityLimits water;
				public PerChunkEntityLimits ambient;
				public PerChunkEntityLimits items;
				public PerChunkEntityLimits xpOrbs;
				
				public static class PerChunkEntityLimits
				{
					public int lowerLimit;
					public int higherLimit;
				}
			}
		}
	}
}
