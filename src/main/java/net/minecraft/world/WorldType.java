package net.minecraft.world;

import java.util.Arrays;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCreateFlatWorld;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderFlat;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerBiome;
import net.minecraft.world.gen.layer.GenLayerBiomeEdge;
import net.minecraft.world.gen.layer.GenLayerZoom;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class WorldType
{
	public static WorldType[] worldTypes = new WorldType[16];
	public static final WorldType DEFAULT = (new WorldType(0, "default", 1)).setVersioned();
	public static final WorldType FLAT = new WorldType(1, "flat");
	public static final WorldType LARGE_BIOMES = new WorldType(2, "largeBiomes");
	public static final WorldType AMPLIFIED = (new WorldType(3, "amplified")).setNotificationData();
	public static final WorldType DEFAULT_1_1 = (new WorldType(8, "default_1_1", 0)).setCanBeCreated(false);
	private final int worldTypeId;
	private final String worldType;
	private final int generatorVersion;
	private boolean canBeCreated;
	private boolean isWorldTypeVersioned;
	private boolean hasNotificationData;
	private static final String __OBFID = "CL_00000150";

	private WorldType(int par1, String par2Str)
	{
		this(par1, par2Str, 0);
	}

	private WorldType(int par1, String par2Str, int par3)
	{
		if (par2Str.length() > 16) throw new IllegalArgumentException("World type names must not be longer then 16: " + par2Str.length());
		this.worldType = par2Str;
		this.generatorVersion = par3;
		this.canBeCreated = true;
		this.worldTypeId = par1;
		worldTypes[par1] = this;
	}

	public String getWorldTypeName()
	{
		return this.worldType;
	}

	@SideOnly(Side.CLIENT)
	public String getTranslateName()
	{
		return "generator." + this.worldType;
	}

	@SideOnly(Side.CLIENT)
	public String func_151359_c()
	{
		return this.getTranslateName() + ".info";
	}

	public int getGeneratorVersion()
	{
		return this.generatorVersion;
	}

	public WorldType getWorldTypeForGeneratorVersion(int par1)
	{
		return this == DEFAULT && par1 == 0 ? DEFAULT_1_1 : this;
	}

	private WorldType setCanBeCreated(boolean par1)
	{
		this.canBeCreated = par1;
		return this;
	}

	@SideOnly(Side.CLIENT)
	public boolean getCanBeCreated()
	{
		return this.canBeCreated;
	}

	private WorldType setVersioned()
	{
		this.isWorldTypeVersioned = true;
		return this;
	}

	public boolean isVersioned()
	{
		return this.isWorldTypeVersioned;
	}

	public static WorldType parseWorldType(String par0Str)
	{
		for (int i = 0; i < worldTypes.length; ++i)
		{
			if (worldTypes[i] != null && worldTypes[i].worldType.equalsIgnoreCase(par0Str))
			{
				return worldTypes[i];
			}
		}

		return null;
	}

	public int getWorldTypeID()
	{
		return this.worldTypeId;
	}

	@SideOnly(Side.CLIENT)
	public boolean showWorldInfoNotice()
	{
		return this.hasNotificationData;
	}

	private WorldType setNotificationData()
	{
		this.hasNotificationData = true;
		return this;
	}

	public WorldChunkManager getChunkManager(World world)
	{
		if (this == FLAT)
		{
			FlatGeneratorInfo flatgeneratorinfo = FlatGeneratorInfo.createFlatGeneratorFromString(world.getWorldInfo().getGeneratorOptions());
			return new WorldChunkManagerHell(BiomeGenBase.getBiome(flatgeneratorinfo.getBiome()), 0.5F);
		}
		else
		{
			return new WorldChunkManager(world);
		}
	}

	public IChunkProvider getChunkGenerator(World world, String generatorOptions)
	{
		return (this == FLAT ? new ChunkProviderFlat(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), generatorOptions) : new ChunkProviderGenerate(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled()));
	}

	public int getMinimumSpawnHeight(World world)
	{
		return this == FLAT ? 4 : 64;
	}

	public double getHorizon(World world)
	{
		return this == FLAT ? 0.0D : 63.0D;
	}

	public boolean hasVoidParticles(boolean flag)
	{
		return this != FLAT && !flag;
	}

	public double voidFadeMagnitude()
	{
		return this == FLAT ? 1.0D : 0.03125D;
	}

/*	  public BiomeGenBase[] getBiomesForWorldType() {
		return biomesForWorldType;
	}

	public void addNewBiome(BiomeGenBase biome)
	{
		Set<BiomeGenBase> newBiomesForWorld = Sets.newLinkedHashSet(Arrays.asList(biomesForWorldType));
		newBiomesForWorld.add(biome);
	   biomesForWorldType = newBiomesForWorld.toArray(new BiomeGenBase[0]);
	}

	public void removeBiome(BiomeGenBase biome)
	{
		Set<BiomeGenBase> newBiomesForWorld = Sets.newLinkedHashSet(Arrays.asList(biomesForWorldType));
		newBiomesForWorld.remove(biome);
		biomesForWorldType = newBiomesForWorld.toArray(new BiomeGenBase[0]);
	}
*/
	public boolean handleSlimeSpawnReduction(Random random, World world)
	{
		return this == FLAT ? random.nextInt(4) != 1 : false;
	}

	/*=================================================== FORGE START ======================================*/
	private static int getNextID()
	{
		for (int x = 0; x < worldTypes.length; x++)
		{
			if (worldTypes[x] == null)
			{
				return x;
			}
		}

		int oldLen = worldTypes.length;
		worldTypes = Arrays.copyOf(worldTypes, oldLen + 16);
		return oldLen;
	}

	/**
	 * Creates a new world type, the ID is hidden and should not be referenced by modders.
	 * It will automatically expand the underlying workdType array if there are no IDs left.
	 * @param name
	 */
	public WorldType(String name)
	{
		this(getNextID(), name);
	}
	
	/**
	 * Called when 'Create New World' button is pressed before starting game
	 */
	public void onGUICreateWorldPress() { }

	/**
	 * Gets the spawn fuzz for players who join the world.
	 * Useful for void world types.
	 * @return Fuzz for entity initial spawn in blocks.
	 */
	public int getSpawnFuzz()
	{
		return 20;
	}

	/**
	 * Called when the 'Customize' button is pressed on world creation GUI
	 * @param instance The minecraft instance
	 * @param guiCreateWorld the createworld GUI
	 */
	@SideOnly(Side.CLIENT)
	public void onCustomizeButton(Minecraft instance, GuiCreateWorld guiCreateWorld)
	{
		if (this == FLAT)
		{
			instance.displayGuiScreen(new GuiCreateFlatWorld(guiCreateWorld, guiCreateWorld.field_146334_a));
		}
	}

	/**
	 * Should world creation GUI show 'Customize' button for this world type?
	 * @return if this world type has customization parameters
	 */
	public boolean isCustomizable()
	{
		return this == FLAT;
	}
	

	/**
	 * Get the height to render the clouds for this world type
	 * @return The height to render clouds at
	 */
	public float getCloudHeight()
	{
		return 128.0F;
	}

	/**
	 * Creates the GenLayerBiome used for generating the world
	 * 
	 * @param worldSeed The world seed
	 * @param parentLayer The parent layer to feed into any layer you return
	 * @return A GenLayer that will return ints representing the Biomes to be generated, see GenLayerBiome
	 */
	public GenLayer getBiomeLayer(long worldSeed, GenLayer parentLayer)
	{
		GenLayer ret = new GenLayerBiome(200L, parentLayer, this);
		ret = GenLayerZoom.magnify(1000L, ret, 2);
		ret = new GenLayerBiomeEdge(1000L, ret);
		return ret;
	}
}