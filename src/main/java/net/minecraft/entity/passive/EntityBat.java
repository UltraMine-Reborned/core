package net.minecraft.entity.passive;

import java.util.Calendar;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityBat extends EntityAmbientCreature
{
	private ChunkCoordinates spawnPosition;
	private static final String __OBFID = "CL_00001637";

	public EntityBat(World par1World)
	{
		super(par1World);
		this.setSize(0.5F, 0.9F);
		this.setIsBatHanging(true);
	}

	protected void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObject(16, new Byte((byte)0));
	}

	protected float getSoundVolume()
	{
		return 0.1F;
	}

	protected float getSoundPitch()
	{
		return super.getSoundPitch() * 0.95F;
	}

	protected String getLivingSound()
	{
		return this.getIsBatHanging() && this.rand.nextInt(4) != 0 ? null : "mob.bat.idle";
	}

	protected String getHurtSound()
	{
		return "mob.bat.hurt";
	}

	protected String getDeathSound()
	{
		return "mob.bat.death";
	}

	public boolean canBePushed()
	{
		return false;
	}

	protected void collideWithEntity(Entity par1Entity) {}

	protected void collideWithNearbyEntities() {}

	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(6.0D);
	}

	public boolean getIsBatHanging()
	{
		return (this.dataWatcher.getWatchableObjectByte(16) & 1) != 0;
	}

	public void setIsBatHanging(boolean par1)
	{
		byte b0 = this.dataWatcher.getWatchableObjectByte(16);

		if (par1)
		{
			this.dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 | 1)));
		}
		else
		{
			this.dataWatcher.updateObject(16, Byte.valueOf((byte)(b0 & -2)));
		}
	}

	protected boolean isAIEnabled()
	{
		return true;
	}

	public void onUpdate()
	{
		super.onUpdate();

		if (this.getIsBatHanging())
		{
			this.motionX = this.motionY = this.motionZ = 0.0D;
			this.posY = (double)MathHelper.floor_double(this.posY) + 1.0D - (double)this.height;
		}
		else
		{
			this.motionY *= 0.6000000238418579D;
		}
	}

	protected void updateAITasks()
	{
		super.updateAITasks();

		if (this.getIsBatHanging())
		{
			if (!this.worldObj.getBlock(MathHelper.floor_double(this.posX), (int)this.posY + 1, MathHelper.floor_double(this.posZ)).isNormalCube())
			{
				this.setIsBatHanging(false);
				this.worldObj.playAuxSFXAtEntity((EntityPlayer)null, 1015, (int)this.posX, (int)this.posY, (int)this.posZ, 0);
			}
			else
			{
				if (this.rand.nextInt(200) == 0)
				{
					this.rotationYawHead = (float)this.rand.nextInt(360);
				}

				if (this.worldObj.getClosestPlayerToEntity(this, 4.0D) != null)
				{
					this.setIsBatHanging(false);
					this.worldObj.playAuxSFXAtEntity((EntityPlayer)null, 1015, (int)this.posX, (int)this.posY, (int)this.posZ, 0);
				}
			}
		}
		else
		{
			if (this.spawnPosition != null && (!this.worldObj.isAirBlock(this.spawnPosition.posX, this.spawnPosition.posY, this.spawnPosition.posZ) || this.spawnPosition.posY < 1))
			{
				this.spawnPosition = null;
			}

			if (this.spawnPosition == null || this.rand.nextInt(30) == 0 || this.spawnPosition.getDistanceSquared((int)this.posX, (int)this.posY, (int)this.posZ) < 4.0F)
			{
				this.spawnPosition = new ChunkCoordinates((int)this.posX + this.rand.nextInt(7) - this.rand.nextInt(7), (int)this.posY + this.rand.nextInt(6) - 2, (int)this.posZ + this.rand.nextInt(7) - this.rand.nextInt(7));
			}

			double d0 = (double)this.spawnPosition.posX + 0.5D - this.posX;
			double d1 = (double)this.spawnPosition.posY + 0.1D - this.posY;
			double d2 = (double)this.spawnPosition.posZ + 0.5D - this.posZ;
			this.motionX += (Math.signum(d0) * 0.5D - this.motionX) * 0.10000000149011612D;
			this.motionY += (Math.signum(d1) * 0.699999988079071D - this.motionY) * 0.10000000149011612D;
			this.motionZ += (Math.signum(d2) * 0.5D - this.motionZ) * 0.10000000149011612D;
			float f = (float)(Math.atan2(this.motionZ, this.motionX) * 180.0D / Math.PI) - 90.0F;
			float f1 = MathHelper.wrapAngleTo180_float(f - this.rotationYaw);
			this.moveForward = 0.5F;
			this.rotationYaw += f1;

			if (this.rand.nextInt(100) == 0 && this.worldObj.getBlock(MathHelper.floor_double(this.posX), (int)this.posY + 1, MathHelper.floor_double(this.posZ)).isNormalCube())
			{
				this.setIsBatHanging(true);
			}
		}
	}

	protected boolean canTriggerWalking()
	{
		return false;
	}

	protected void fall(float par1) {}

	protected void updateFallState(double par1, boolean par3) {}

	public boolean doesEntityNotTriggerPressurePlate()
	{
		return true;
	}

	public boolean attackEntityFrom(DamageSource par1DamageSource, float par2)
	{
		if (this.isEntityInvulnerable())
		{
			return false;
		}
		else
		{
			if (!this.worldObj.isRemote && this.getIsBatHanging())
			{
				this.setIsBatHanging(false);
			}

			return super.attackEntityFrom(par1DamageSource, par2);
		}
	}

	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readEntityFromNBT(par1NBTTagCompound);
		this.dataWatcher.updateObject(16, Byte.valueOf(par1NBTTagCompound.getByte("BatFlags")));
	}

	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeEntityToNBT(par1NBTTagCompound);
		par1NBTTagCompound.setByte("BatFlags", this.dataWatcher.getWatchableObjectByte(16));
	}

	public boolean getCanSpawnHere()
	{
		int i = MathHelper.floor_double(this.boundingBox.minY);

		if (i >= 63)
		{
			return false;
		}
		else
		{
			int j = MathHelper.floor_double(this.posX);
			int k = MathHelper.floor_double(this.posZ);
			int l = this.worldObj.getBlockLightValue(j, i, k);
			byte b0 = 4;
			Calendar calendar = this.worldObj.getCurrentDate();

			if ((calendar.get(2) + 1 != 10 || calendar.get(5) < 20) && (calendar.get(2) + 1 != 11 || calendar.get(5) > 3))
			{
				if (this.rand.nextBoolean())
				{
					return false;
				}
			}
			else
			{
				b0 = 7;
			}

			return l > this.rand.nextInt(b0) ? false : super.getCanSpawnHere();
		}
	}
}