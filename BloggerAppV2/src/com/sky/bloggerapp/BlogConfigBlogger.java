package com.sky.bloggerapp;

import java.util.LinkedList;

public class BlogConfigBlogger
{
	public enum BlogInterfaceType {
		BLOGGER, UNKNOWN
	}

	private final static int UNKNOWN_CONFIG_TYPE = 0xFFFF;
	public final static String APPNAME = "AndroBlogger";
	public final static char FIELD_DELIMITER = '|';

	public static String typeConstantTitle(BlogInterfaceType type)
	{
		String res = null;
		switch (type)
		{
			case BLOGGER:
				res = "Blogger / API";
				break;
			default:
				res = "Incompatible";
				break;
		}
		return res;
	}

	public static LinkedList<String> typeConstantTitles()
	{
		BlogInterfaceType[] arr = BlogInterfaceType.values();
		LinkedList<String> res = new LinkedList<String>();
		for (int i = 0; i < arr.length; i++)
		{
			if (arr[i] != BlogInterfaceType.UNKNOWN)
			{
				res.add(typeConstantTitle(arr[i]));
			}
		}
		return res;
	}

	public static String typeConstantDesc(BlogInterfaceType type)
	{
		String base = "You need to check your blog service provider for " + "information which API they support. Blogger API is " + "supported by BlogSpot and many other online blogging " + "services.";
		switch (type)
		{
			case BLOGGER:
				return "Blog uses Blogger API (Google Data API). " + base;
			default:
				return "Unknown protocol. " + base;
		}
	}

	public static int getInterfaceNumberByType(BlogInterfaceType type)
	{
		switch (type)
		{
			case BLOGGER:
				return 1;
			default:
				return UNKNOWN_CONFIG_TYPE;
		}
	}

	public static BlogInterfaceType getInterfaceTypeByNumber(int configNum)
	{
		switch (configNum)
		{
			case 1:
				return BlogInterfaceType.BLOGGER;
			case UNKNOWN_CONFIG_TYPE:
			default:
				return BlogInterfaceType.UNKNOWN;
		}
	}

}
