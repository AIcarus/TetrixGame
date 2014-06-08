package com.accela.tetrixgame.gamelogic.uisupport;

import java.net.URL;

/**
 * 
 * 为游戏的绘制提供定位资源的方法
 * 
 */
public class ResourceLocator
{
	public static URL getURL(String dir)
	{
		if (null == dir)
		{
			throw new NullPointerException("dir should not be null");
		}

		URL result = ResourceLocator.class.getClassLoader().getResource(dir);
		if (null == result)
		{
			throw new IllegalArgumentException(
					"dir is illegal or file not exists");
		}

		return result;
	}
}
