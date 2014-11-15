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
import android.database.Cursor;
import android.provider.BaseColumns;

import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.AreasColumns;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.AreasSchema;
import org.bouldermountainbike.smarttrail.ui.SetupFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

/**
 * Area represents a trail area. There can be many trails in an area and many
 * areas in a region.
 * 
 * @author matt
 * 
 */
public class Area implements AreasColumns, BaseColumns {
	public static final String BBOX = "bbox";

	public String mId; // unique string id of the area.
	public String mName;
	public String mOwner;
	public String mDescription;
	public String mColor;
	public int mNumReviews;
	public float mRating;
	public long mUpdatedAt;
	public LatLng mCenter;
	public LatLng[] mBbox = new LatLng[2];
	public int mVersion;

	public PolygonOptions mMapBox = new PolygonOptions();

	public Area() {
	}

	public Area(JSONObject area) throws JSONException {
		//
		// Required
		//
		mId = area.getString(AREA_ID);
		mName = area.getString(NAME);
		mOwner = area.getString(OWNER);
		mDescription = area.getString(DESCRIPTION);
		mUpdatedAt = area.getLong(UPDATED_AT);
		mVersion = area.optInt(VERSION,SetupFragment.MAPS_VERSION);

		JSONArray data = area.getJSONArray(BBOX);
		setBbox(data.getDouble(0), data.getDouble(1), data.getDouble(2),
				data.getDouble(3));
	}

	public JSONObject toJson() throws JSONException {

		JSONObject area = new JSONObject();

		area.put(AREA_ID, mId);
		area.put(NAME, mName);
		area.put(OWNER, mOwner);
		area.put(DESCRIPTION, mDescription);
		area.put(BBOX, new double[] { mBbox[0].longitude, mBbox[0].latitude,
				mBbox[1].longitude, mBbox[1].latitude });
		area.put(VERSION, mVersion);

		return area;
	}

	public ContentValues getValues() {
		ContentValues values = new ContentValues();

		values.put(AREA_ID, mId);
		values.put(NAME, mName);
		values.put(OWNER, mOwner);
		values.put(DESCRIPTION, mDescription);
		values.put(VERSION, mVersion);
		values.put(BBOX_LAT_0, mBbox[0].latitude);
		values.put(BBOX_LON_0, mBbox[0].longitude);
		values.put(BBOX_LAT_1, mBbox[1].latitude);
		values.put(BBOX_LON_1, mBbox[1].longitude);

		return values;
	}

	public String insert(ContentResolver provider) {
		String id = AreasSchema.insert(provider, getValues());
		mId = id;
		return id;
	}

	public int update(ContentResolver provider) {
		return AreasSchema.update(provider, mId, getValues());
	}


	public String upsert(ContentResolver provider) {

		Cursor cursor = provider.query(AreasSchema.CONTENT_URI,
				new String[] { AreasColumns.AREA_ID }, AreasColumns.AREA_ID
						+ "='" + mId + "'", null, null);

		try {
			if (cursor == null || cursor.getCount() == 0) {
				return insert(provider); // database replaces on conflict
			} else {
				update(provider);
				return mId;
			}
		} finally {
			cursor.close();
		}
	}

	public void setBbox(double lon0, double lat0, double lon1, double lat1) {
		mBbox[0] = new LatLng(lat0, lon0);
		mBbox[1] = new LatLng(lat1, lon1);
		double latitude = 0.5 * (mBbox[0].latitude + mBbox[1].latitude);
		double longitude = 0.5 * (mBbox[0].longitude + mBbox[1].longitude);

		mCenter = new LatLng(latitude, longitude);

		mMapBox.add(mBbox[0])
				.add(new LatLng(mBbox[1].latitude, mBbox[0].longitude))
				.add(mBbox[1])
				.add(new LatLng(mBbox[0].latitude, mBbox[1].longitude));
		mMapBox.strokeWidth(1.0f);
		mMapBox.fillColor(0x10101010);
		
	}
}
