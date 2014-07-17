package net.minecraftforge.client;

import java.util.Random;

import javax.imageio.ImageIO;

import net.minecraftforge.client.event.MouseEvent;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundEventAccessorComposite;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.FOVUpdateEvent;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.PixelFormat;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLLog;
import net.minecraft.client.Minecraft;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderWorldEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent17;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.Status;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.RenderBlockFluid;
import static net.minecraftforge.client.IItemRenderer.ItemRenderType.*;
import static net.minecraftforge.client.IItemRenderer.ItemRendererHelper.*;
import static net.minecraftforge.common.ForgeVersion.Status.*;

public class ForgeHooksClient
{
	//private static final ResourceLocation ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

	static TextureManager engine()
	{
		return FMLClientHandler.instance().getClient().renderEngine;
	}

	public static String getArmorTexture(Entity entity, ItemStack armor, String _default, int slot, String type)
	{
		String result = armor.getItem().getArmorTexture(armor, entity, slot, type);
		return result != null ? result : _default;
	}

	public static boolean renderEntityItem(EntityItem entity, ItemStack item, float bobing, float rotation, Random random, TextureManager engine, RenderBlocks renderBlocks, int count)
	{
		IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(item, ENTITY);
		if (customRenderer == null)
		{
			return false;
		}

		if (customRenderer.shouldUseRenderHelper(ENTITY, item, ENTITY_ROTATION))
		{
			GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
		}
		if (!customRenderer.shouldUseRenderHelper(ENTITY, item, ENTITY_BOBBING))
		{
			GL11.glTranslatef(0.0F, -bobing, 0.0F);
		}
		boolean is3D = customRenderer.shouldUseRenderHelper(ENTITY, item, BLOCK_3D);

		engine.bindTexture(item.getItemSpriteNumber() == 0 ? TextureMap.locationBlocksTexture : TextureMap.locationItemsTexture);
		Block block = item.getItem() instanceof ItemBlock ? Block.getBlockFromItem(item.getItem()) : null;
		if (is3D || (block != null && RenderBlocks.renderItemIn3d(block.getRenderType())))
		{
			int renderType = (block != null ? block.getRenderType() : 1);
			float scale = (renderType == 1 || renderType == 19 || renderType == 12 || renderType == 2 ? 0.5F : 0.25F);
			boolean blend = block != null && block.getRenderBlockPass() > 0;

			if (RenderItem.renderInFrame)
			{
				GL11.glScalef(1.25F, 1.25F, 1.25F);
				GL11.glTranslatef(0.0F, 0.05F, 0.0F);
				GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			}

			if (blend)
			{
				GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
				GL11.glEnable(GL11.GL_BLEND);
				OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			}

			GL11.glScalef(scale, scale, scale);

			for(int j = 0; j < count; j++)
			{
				GL11.glPushMatrix();
				if (j > 0)
				{
					GL11.glTranslatef(
						((random.nextFloat() * 2.0F - 1.0F) * 0.2F) / scale,
						((random.nextFloat() * 2.0F - 1.0F) * 0.2F) / scale,
						((random.nextFloat() * 2.0F - 1.0F) * 0.2F) / scale);
				}
				customRenderer.renderItem(ENTITY, item, renderBlocks, entity);
				GL11.glPopMatrix();
			}

			if (blend)
			{
				GL11.glDisable(GL11.GL_BLEND);
			}
		}
		else
		{
			GL11.glScalef(0.5F, 0.5F, 0.5F);
			customRenderer.renderItem(ENTITY, item, renderBlocks, entity);
		}
		return true;
	}

	public static boolean renderInventoryItem(RenderBlocks renderBlocks, TextureManager engine, ItemStack item, boolean inColor, float zLevel, float x, float y)
	{
		IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(item, INVENTORY);
		if (customRenderer == null)
		{
			return false;
		}

		engine.bindTexture(item.getItemSpriteNumber() == 0 ? TextureMap.locationBlocksTexture : TextureMap.locationItemsTexture);
		if (customRenderer.shouldUseRenderHelper(INVENTORY, item, INVENTORY_BLOCK))
		{
			GL11.glPushMatrix();
			GL11.glTranslatef(x - 2, y + 3, -3.0F + zLevel);
			GL11.glScalef(10F, 10F, 10F);
			GL11.glTranslatef(1.0F, 0.5F, 1.0F);
			GL11.glScalef(1.0F, 1.0F, -1F);
			GL11.glRotatef(210F, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(45F, 0.0F, 1.0F, 0.0F);

			if(inColor)
			{
				int color = item.getItem().getColorFromItemStack(item, 0);
				float r = (float)(color >> 16 & 0xff) / 255F;
				float g = (float)(color >> 8 & 0xff) / 255F;
				float b = (float)(color & 0xff) / 255F;
				GL11.glColor4f(r, g, b, 1.0F);
			}

			GL11.glRotatef(-90F, 0.0F, 1.0F, 0.0F);
			renderBlocks.useInventoryTint = inColor;
			customRenderer.renderItem(INVENTORY, item, renderBlocks);
			renderBlocks.useInventoryTint = true;
			GL11.glPopMatrix();
		}
		else
		{
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glPushMatrix();
			GL11.glTranslatef(x, y, -3.0F + zLevel);

			if (inColor)
			{
				int color = item.getItem().getColorFromItemStack(item, 0);
				float r = (float)(color >> 16 & 255) / 255.0F;
				float g = (float)(color >> 8 & 255) / 255.0F;
				float b = (float)(color & 255) / 255.0F;
				GL11.glColor4f(r, g, b, 1.0F);
			}

			customRenderer.renderItem(INVENTORY, item, renderBlocks);
			GL11.glPopMatrix();
			GL11.glEnable(GL11.GL_LIGHTING);
		}

		return true;
	}

	public static void renderEffectOverlay(TextureManager manager, RenderItem render)
	{
	}

	public static void renderEquippedItem(ItemRenderType type, IItemRenderer customRenderer, RenderBlocks renderBlocks, EntityLivingBase entity, ItemStack item)
	{
		if (customRenderer.shouldUseRenderHelper(type, item, EQUIPPED_BLOCK))
		{
			GL11.glPushMatrix();
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
			customRenderer.renderItem(type, item, renderBlocks, entity);
			GL11.glPopMatrix();
		}
		else
		{
			GL11.glPushMatrix();
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GL11.glTranslatef(0.0F, -0.3F, 0.0F);
			GL11.glScalef(1.5F, 1.5F, 1.5F);
			GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
			GL11.glTranslatef(-0.9375F, -0.0625F, 0.0F);
			customRenderer.renderItem(type, item, renderBlocks, entity);
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			GL11.glPopMatrix();
		}
	}

	//Optifine Helper Functions u.u, these are here specifically for Optifine
	//Note: When using Optfine, these methods are invoked using reflection, which
	//incurs a major performance penalty.
	public static void orientBedCamera(Minecraft mc, EntityLivingBase entity)
	{
		int x = MathHelper.floor_double(entity.posX);
		int y = MathHelper.floor_double(entity.posY);
		int z = MathHelper.floor_double(entity.posZ);
		Block block = mc.theWorld.getBlock(x, y, z);

		if (block != null && block.isBed(mc.theWorld, x, y, z, entity))
		{
			GL11.glRotatef((float)(block.getBedDirection(mc.theWorld, x, y, z) * 90), 0.0F, 1.0F, 0.0F);
		}
	}

	public static boolean onDrawBlockHighlight(RenderGlobal context, EntityPlayer player, MovingObjectPosition target, int subID, ItemStack currentItem, float partialTicks)
	{
		return MinecraftForge.EVENT_BUS.post(new DrawBlockHighlightEvent(context, player, target, subID, currentItem, partialTicks));
	}

	public static void dispatchRenderLast(RenderGlobal context, float partialTicks)
	{
		MinecraftForge.EVENT_BUS.post(new RenderWorldLastEvent(context, partialTicks));
	}

	public static boolean renderFirstPersonHand(RenderGlobal context, float partialTicks, int renderPass)
	{
		return MinecraftForge.EVENT_BUS.post(new RenderHandEvent(context, partialTicks, renderPass));
	}

	public static void onTextureStitchedPre(TextureMap map)
	{
		MinecraftForge.EVENT_BUS.post(new TextureStitchEvent.Pre(map));
	}

	public static void onTextureStitchedPost(TextureMap map)
	{
		MinecraftForge.EVENT_BUS.post(new TextureStitchEvent.Post(map));

		FluidRegistry.WATER.setIcons(BlockLiquid.getLiquidIcon("water_still"), BlockLiquid.getLiquidIcon("water_flow"));
		FluidRegistry.LAVA.setIcons(BlockLiquid.getLiquidIcon("lava_still"), BlockLiquid.getLiquidIcon("lava_flow"));
	}

	/**
	 * This is added for Optifine's convenience. And to explode if a ModMaker is developing.
	 * @param texture
	 */
	public static void onTextureLoadPre(String texture)
	{
		if (Tessellator.renderingWorldRenderer)
		{
			FMLLog.warning("Warning: Texture %s not preloaded, will cause render glitches!", texture);
			if (Tessellator.class.getPackage() != null)
			{
				if (Tessellator.class.getPackage().getName().startsWith("net.minecraft."))
				{
					Minecraft mc = FMLClientHandler.instance().getClient();
					if (mc.ingameGUI != null)
					{
						mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentTranslation("forge.texture.preload.warning", texture));
					}
				}
			}
		}
	}

	static int renderPass = -1;
	public static void setRenderPass(int pass)
	{
		renderPass = pass;
	}

	public static ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, int slotID, ModelBiped _default)
	{
		ModelBiped modelbiped = itemStack.getItem().getArmorModel(entityLiving, itemStack, slotID);
		return modelbiped == null ? _default : modelbiped;
	}

	static int stencilBits = 0;
	public static void createDisplay() throws LWJGLException
	{
		ImageIO.setUseCache(false); //Disable on-disc stream cache should speed up texture pack reloading.
		PixelFormat format = new PixelFormat().withDepthBits(24);
		if (!ForgeModContainer.enableStencilBits || Boolean.parseBoolean(System.getProperty("forge.forceNoStencil", "false")))
		{
			Display.create(format);
			stencilBits = 0;
			return;
		}
		try
		{
			//TODO: Figure out how to determine the max bits.
			Display.create(format.withStencilBits(8));
			stencilBits = 8;
		}
		catch(LWJGLException e)
		{
			Display.create(format);
			stencilBits = 0;
		}
	}

	//This properly moves the domain, if provided, to the front of the string before concatenating
	public static String fixDomain(String base, String complex)
	{
		int idx = complex.indexOf(':');
		if (idx == -1)
		{
			return base + complex;
		}

		String name = complex.substring(idx + 1, complex.length());
		if (idx > 1)
		{
			String domain = complex.substring(0, idx);
			return domain + ':' + base + name;
		}
		else
		{
			return base + name;
		}
	}

	public static boolean postMouseEvent()
	{
		return MinecraftForge.EVENT_BUS.post(new MouseEvent());
	}

	public static float getOffsetFOV(EntityPlayerSP entity, float fov)
	{
		FOVUpdateEvent fovUpdateEvent = new FOVUpdateEvent(entity, fov);
		MinecraftForge.EVENT_BUS.post(fovUpdateEvent);
		return fovUpdateEvent.newfov;
	}

	private static int skyX, skyZ;

	private static boolean skyInit;
	private static int skyRGBMultiplier;

	public static int getSkyBlendColour(World world, int playerX, int playerY, int playerZ)
	{
		if (playerX == skyX && playerZ == skyZ && skyInit)
		{
			return skyRGBMultiplier;
		}
		skyInit = true;

		GameSettings settings = Minecraft.getMinecraft().gameSettings;
		int[] ranges = ForgeModContainer.blendRanges;
		int distance = 0;
		if (settings.fancyGraphics && settings.renderDistanceChunks >= 0 && settings.renderDistanceChunks < ranges.length)
		{
			distance = ranges[settings.renderDistanceChunks];
		}

		int r = 0;
		int g = 0;
		int b = 0;

		int divider = 0;
		for (int x = -distance; x <= distance; ++x)
		{
			for (int z = -distance; z <= distance; ++z)
			{
				BiomeGenBase biome = world.getBiomeGenForCoords(playerX + x, playerZ + z);
				int colour = biome.getSkyColorByTemp(biome.getFloatTemperature(playerX + x, playerY, playerZ + z));
				r += (colour & 0xFF0000) >> 16;
				g += (colour & 0x00FF00) >> 8;
				b += colour & 0x0000FF;
				divider++;
			}
		}

		int multiplier = (r / divider & 255) << 16 | (g / divider & 255) << 8 | b / divider & 255;

		skyX = playerX;
		skyZ = playerZ;
		skyRGBMultiplier = multiplier;
		return skyRGBMultiplier;
	}
	/**
	 * Initialization of Forge Renderers.
	 */
	static
	{
		FluidRegistry.renderIdFluid = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(RenderBlockFluid.instance);
	}

	public static void renderMainMenu(GuiMainMenu gui, FontRenderer font, int width, int height)
	{
		Status status = ForgeVersion.getStatus();
		if (status == BETA || status == BETA_OUTDATED)
		{
			// render a warning at the top of the screen,
			String line = I18n.format("forge.update.beta.1", EnumChatFormatting.RED, EnumChatFormatting.RESET);
			gui.drawString(font, line, (width - font.getStringWidth(line)) / 2, 4 + (0 * (font.FONT_HEIGHT + 1)), -1);
			line = I18n.format("forge.update.beta.2");
			gui.drawString(font, line, (width - font.getStringWidth(line)) / 2, 4 + (1 * (font.FONT_HEIGHT + 1)), -1);
		}

		String line = null;
		switch(status)
		{
			//case FAILED:        line = " Version check failed"; break;
			//case UP_TO_DATE:    line = "Forge up to date"}; break;
			//case AHEAD:         line = "Using non-recommended Forge build, issues may arise."}; break;
			case OUTDATED:
			case BETA_OUTDATED: line = I18n.format("forge.update.newversion", ForgeVersion.getTarget()); break;
			default: break;
		}

		if (line != null)
		{
			// if we have a line, render it in the bottom right, above Mojang's copyright line
			gui.drawString(font, line, width - font.getStringWidth(line) - 2, height - (2 * (font.FONT_HEIGHT + 1)), -1);
		}
	}

	public static ISound playSound(SoundManager manager, ISound sound)
	{
		SoundEventAccessorComposite accessor = manager.sndHandler.getSound(sound.getPositionedSoundLocation());
		PlaySoundEvent17 e = new PlaySoundEvent17(manager, sound, (accessor == null ? null : accessor.getSoundCategory()));
		MinecraftForge.EVENT_BUS.post(e);
		return e.result;
	}

	static RenderBlocks worldRendererRB;
	static int worldRenderPass;

	public static int getWorldRenderPass()
	{
		return worldRenderPass;
	}

	public static void setWorldRendererRB(RenderBlocks renderBlocks)
	{
		worldRendererRB = renderBlocks;
	}

	public static void onPreRenderWorld(WorldRenderer worldRenderer, int pass)
	{
		if(worldRendererRB != null)
		{
			worldRenderPass = pass;
			MinecraftForge.EVENT_BUS.post(new RenderWorldEvent.Pre(worldRenderer, (ChunkCache)worldRendererRB.blockAccess, worldRendererRB, pass));
		}
	}

	public static void onPostRenderWorld(WorldRenderer worldRenderer, int pass)
	{
		if(worldRendererRB != null)
		{
			MinecraftForge.EVENT_BUS.post(new RenderWorldEvent.Post(worldRenderer, (ChunkCache)worldRendererRB.blockAccess, worldRendererRB, pass));
			worldRenderPass = -1;
		}
	}
}
