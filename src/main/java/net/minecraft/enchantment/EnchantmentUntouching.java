package net.minecraft.enchantment;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class EnchantmentUntouching extends Enchantment
{
	private static final String __OBFID = "CL_00000123";

	protected EnchantmentUntouching(int par1, int par2)
	{
		super(par1, par2, EnumEnchantmentType.digger);
		this.setName("untouching");
	}

	public int getMinEnchantability(int par1)
	{
		return 15;
	}

	public int getMaxEnchantability(int par1)
	{
		return super.getMinEnchantability(par1) + 50;
	}

	public int getMaxLevel()
	{
		return 1;
	}

	public boolean canApplyTogether(Enchantment par1Enchantment)
	{
		return super.canApplyTogether(par1Enchantment) && par1Enchantment.effectId != fortune.effectId;
	}

	public boolean canApply(ItemStack par1ItemStack)
	{
		return par1ItemStack.getItem() == Items.shears ? true : super.canApply(par1ItemStack);
	}
}