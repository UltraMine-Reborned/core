package net.minecraft.enchantment;

public class EnchantmentFireAspect extends Enchantment
{
	private static final String __OBFID = "CL_00000116";

	protected EnchantmentFireAspect(int par1, int par2)
	{
		super(par1, par2, EnumEnchantmentType.weapon);
		this.setName("fire");
	}

	public int getMinEnchantability(int par1)
	{
		return 10 + 20 * (par1 - 1);
	}

	public int getMaxEnchantability(int par1)
	{
		return super.getMinEnchantability(par1) + 50;
	}

	public int getMaxLevel()
	{
		return 2;
	}
}