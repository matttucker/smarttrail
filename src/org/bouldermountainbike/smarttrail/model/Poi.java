/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package org.bouldermountainbike.smarttrail.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.provider.BaseColumns;

import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.PoisColumns;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.PoisSchema;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Poi is a point of interest associated with a trail.
 * 
 * @author matt
 * 
 */
public class Poi implements BaseColumns, PoisColumns {

	private static final String LOCATION = "location";

	public long mId;
	public String mRegionId;
	public String mAreaId;
	public String mTrailId;
	public int mIndex; // trailId/index uniquely identify the poi

	public String mName;
	public String mDescription;

	public MarkerOptions mMarker;

	public Poi() {
		mMarker = new MarkerOptions();
	}

	public Poi(String regionId, String areaId, String trailId, int index,
			JSONObject poi) throws JSONException {

		mRegionId = regionId;
		mAreaId = areaId;
		mTrailId = trailId;
		mIndex = index;

		//
		// Required
		//

		mName = poi.getString(NAME);
		mDescription = poi.getString(DESCRIPTION);

		JSONArray location = poi.getJSONArray(LOCATION);
		double lon = location.getDouble(0);
		double lat = location.getDouble(1);
		LatLng position = new LatLng(lat, lon);
		mMarker = new MarkerOptions().position(position).snippet(mDescription);

	}

	public JSONObject toJson() throws JSONException {

		JSONObject trail = new JSONObject();

		trail.put(TRAIL_ID, mTrailId);
		trail.put(REGION_ID, mRegionId);
		trail.put(AREA_ID, mAreaId);
		trail.put(NAME, mName);
		trail.put(DESCRIPTION, mDescription);

		JSONArray location = new JSONArray();
		location.put(0, mMarker.getPosition().longitude);
		location.put(1, mMarker.getPosition().latitude);
		trail.put(LOCATION, location);

		return trail;
	}

	public ContentValues getValues() {
		ContentValues values = new ContentValues();

		values.put(TRAIL_ID, mTrailId);
		values.put(REGION_ID, mRegionId);
		values.put(AREA_ID, mAreaId);
		values.put(INDEX, mIndex);
		values.put(NAME, mName);
		values.put(DESCRIPTION, mDescription);
		values.put(LAT, mMarker.getPosition().latitude);
		values.put(LON, mMarker.getPosition().longitude);

		return values;
	}

	public long insert(ContentResolver provider) {
		long id = PoisSchema.insert(provider, getValues());
		return id;
	}

	public long update(ContentResolver provider) {
		return PoisSchema.update(provider, mId, getValues());
	}

}
