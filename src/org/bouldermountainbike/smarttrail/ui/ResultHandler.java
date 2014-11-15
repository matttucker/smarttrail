/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package org.bouldermountainbike.smarttrail.ui;

interface ResultHandler {
	boolean getAndClearRefresh();
	void setRefresh(boolean refresh);
}
