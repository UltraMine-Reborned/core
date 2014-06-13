package org.ultramine.server.util;

public class BasicTypeParser
{
	public static boolean isInt(String val)
	{
		int len = val.length();
		if(len > 11 || len == 0) return false;
		if(len > 9)
		{
			try
			{
				Integer.parseInt(val);
				return true;
			} catch(NumberFormatException e){return false;}
		}
		
		int i = 0;
		if(val.charAt(0) == '-')
		{
			i = 1;
			if(len == 1) return false;
		}
		
		for(; i < len; i++)
		{
			char c = val.charAt(i);
			if(c < '0' || c > '9') return false;
		}
		
		return true;
	}
	
	public static boolean isUnsignedInt(String val)
	{
		int len = val.length();
		if(len > 10 || len == 0) return false;
		if(len == 10)
		{
			try
			{
				if(Integer.parseInt(val) >= 0) return true;
			} catch(NumberFormatException e){return false;}
		}
		
		for(int i = 0; i < len; i++)
		{
			char c = val.charAt(i);
			if(c < '0' || c > '9') return false;
		}
		
		return true;
	}
}