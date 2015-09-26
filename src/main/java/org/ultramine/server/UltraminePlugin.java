package org.ultramine.server;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.io.File;
import java.util.Map;

import net.minecraft.launchwrapper.LaunchClassLoader;

public class UltraminePlugin implements IFMLLoadingPlugin
{
	public static File location;

	@Override
	public String[] getASMTransformerClass()
	{
		return new String[]{
				"org.ultramine.server.asm.transformers.TrigMathTransformer",
				"org.ultramine.server.asm.transformers.PrintStackTraceTransformer",
		};
	}

	@Override
	public String getModContainerClass()
	{
		return "org.ultramine.server.UltramineServerModContainer";
	}

	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data)
	{
		location = (File)data.get("coremodLocation");
		LaunchClassLoader cl = (LaunchClassLoader)this.getClass().getClassLoader();
		cl.addTransformerExclusion("org.ultramine.server.asm.");
		
		cl.addTransformerExclusion("org.apache.commons.dbcp2.");
		cl.addTransformerExclusion("org.apache.commons.pool2.");
		cl.addTransformerExclusion("org.apache.commons.logging.");
	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}
}
