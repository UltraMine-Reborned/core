/*
 * Forge Mod Loader
 * Copyright (c) 2012-2013 cpw.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 *	   cpw - implementation
 */

package cpw.mods.fml.client;

import java.io.File;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiBackupFailed extends GuiScreen
{
	private GuiScreen parent;
	private File zipName;
	public GuiBackupFailed(GuiScreen parent, File zipName)
	{
		this.parent = parent;
		this.zipName = zipName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		this.buttonList.add(new GuiButton(1, this.width / 2 - 75, this.height - 38, I18n.format("gui.done")));
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton)
	{
		if (par1GuiButton.enabled && par1GuiButton.id == 1)
		{
			FMLClientHandler.instance().showGuiScreen(parent);
		}
	}
	@Override
	public void drawScreen(int par1, int par2, float par3)
	{
		this.drawDefaultBackground();
		int offset = Math.max(85 - 2 * 10, 10);
		this.drawCenteredString(this.fontRendererObj, String.format("There was an error saving the archive %s", zipName.getName()), this.width / 2, offset, 0xFFFFFF);
		offset += 10;
		this.drawCenteredString(this.fontRendererObj, String.format("Please fix the problem and try again"), this.width / 2, offset, 0xFFFFFF);
		super.drawScreen(par1, par2, par3);
	}
}