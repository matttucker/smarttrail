package org.bouldermountainbike.smarttrail.app;

import org.bouldermountainbike.smarttrail.util.TimeUtil;

public class Config {

	public static final boolean DEBUG = false;
	public static final boolean USE_DEBUG_SERVER = false;

//	public static final String PRODUCTION_DOMAIN = "dev.bouldermountainbike.org";
	public static final String PRODUCTION_DOMAIN = "bouldermountainbike.org";
	public static final String DEBUG_DOMAIN = "192.168.1.109:5000";
	public static final String DOMAIN = USE_DEBUG_SERVER ? DEBUG_DOMAIN : PRODUCTION_DOMAIN;
	public static final boolean USE_SSL = USE_DEBUG_SERVER ? false : true;
			
	public static final String DEFAULT_AREA = "9";
	public static final String DEFAULT_TRAIL = "260";
	
	public static final long UNKNOWN_WINDOW_MS = 3*TimeUtil.DAY_MS;
	public static final String DEFAULT_ALERTS_URL = "http://bouldermountainbike.org/content/trails/news";
	public static final String DEFAULT_TWITTER_QUERY = "#boco_trails OR #valmontbikepark OR boulderbma";
	
}