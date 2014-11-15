/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package org.bouldermountainbike.smarttrail.util;


public class UnitsUtil {

	private static final float METERS_PER_MILE = 1609.344f;
	private static final float MILES_PER_METER = 1.0f/METERS_PER_MILE;
	private static final float FEET_PER_METER = 3.281f;
	private static final float METERS_PER_FEET = 1.0f/FEET_PER_METER;

	public static float metersToMiles(float lengthMeters) {
		return lengthMeters*MILES_PER_METER;
	}

	public static float metersToFeet(float gainMeters) {
		return gainMeters*FEET_PER_METER;
	}
	public static float feetToMeters(float feet) {
		return feet*METERS_PER_FEET;
	}

	public static float milesToMeters(float miles) {
		return miles*METERS_PER_MILE;
	}


}
