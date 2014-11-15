package org.bouldermountainbike.smarttrail.model;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.bouldermountainbike.smarttrail.app.SmartTrailApplication;
import org.bouldermountainbike.smarttrail.error.CredentialsException;
import org.bouldermountainbike.smarttrail.error.SmartTrailException;
import org.bouldermountainbike.smarttrail.map.MapTrail;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.AreasColumns;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.RegionsSchema;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.TrailsColumns;

public class MapData {
	private static final String TAG = "MapData";

	public String mRegionId;

	public ArrayList<MapTrail> mTrails;
	public ArrayList<Area> mAreas;
	public ArrayList<Poi> mPois;

	public MapData(String regionId) {
		mRegionId = regionId;
		mTrails = new ArrayList<MapTrail>();
		mAreas = new ArrayList<Area>();
	}

	/**
	 * Load trails for the selected area.
	 * 
	 * @param context
	 */
	public void readTrailData(Context context, String areaId) {
		ContentResolver resolver = context.getContentResolver();
		mTrails.clear();

		Uri uri = RegionsSchema.buildTrailsUri(mRegionId);
		String where = "trails." + TrailsColumns.AREA_ID + " = '" + areaId
				+ "'";

		Cursor cursor = resolver
				.query(uri, TrailsQuery.PROJECTION, where, null, "trails."
						+ TrailsColumns.NAME + " COLLATE LOCALIZED ASC");

		if (cursor != null) {
			String areaName;
			try {
				while (cursor.moveToNext()) {
					Trail trail = new Trail();

					trail.mName = cursor.getString(TrailsQuery.NAME);
					trail.mId = cursor.getString(TrailsQuery.TRAIL_ID);
					trail.mAreaId = cursor.getString(TrailsQuery.AREA_ID);
					trail.mStatus = cursor.getString(TrailsQuery.CONDITION);
					trail.mDifficultyRating = cursor.getInt(TrailsQuery.DIFFICULTY_RATING);
					trail.mMapJson = cursor.getString(TrailsQuery.MAP);
					trail.mTrailheadsJson = cursor
							.getString(TrailsQuery.TRAILHEADS);
					areaName = cursor.getString(TrailsQuery.AREA_NAME);

					MapTrail mapTrail = new MapTrail(context, trail, areaName);
					mTrails.add(mapTrail);

				}

			} finally {
				cursor.close();
			}

		}
	}

//	/**
//	 * Load trails for the selected area.
//	 * 
//	 * @param context
//	 */
//	public void readAreaData(Context context) {
//		ContentResolver resolver = context.getContentResolver();
//		mTrails.clear();
//
//		Uri uri = RegionsSchema.buildAreasUri(mRegionId);
//		// String areaId = "9";
//		// String where = "trails."+TrailsColumns.AREA_ID + " = '" + areaId +
//		// "'";
//
//		String where = null;
//
//		Cursor cursor = resolver
//				.query(uri, TrailsQuery.PROJECTION, where, null, "trails."
//						+ TrailsColumns.NAME + " COLLATE LOCALIZED ASC");
//
//		if (cursor != null) {
//			String areaName;
//			try {
//				while (cursor.moveToNext()) {
//					Trail trail = new Trail();
//
////					trail.mName = cursor.getString(TrailsQuery.NAME);
////					trail.mId = cursor.getString(TrailsQuery.TRAIL_ID);
////					trail.mStatus = cursor.getString(TrailsQuery.CONDITION);
////					String jsonTrail = cursor.getString(TrailsQuery.MAP);
////					String jsonTrailheads = cursor
////							.getString(TrailsQuery.TRAILHEADS);
////					areaName = cursor.getString(TrailsQuery.AREA_NAME);
////
////					try {
////						trail.inflateTrail(context, jsonTrail);
////						trail.inflateTrailheads(areaName, jsonTrailheads);
////						mTrails.add(trail);
////					} catch (JSONException e) {
////						e.printStackTrace();
////					}
//					trail.mName = cursor.getString(TrailsQuery.NAME);
//					trail.mId = cursor.getString(TrailsQuery.TRAIL_ID);
//					trail.mAreaId = cursor.getString(TrailsQuery.AREA_ID);
//					trail.mStatus = cursor.getString(TrailsQuery.CONDITION);
//					trail.mMapJson = cursor.getString(TrailsQuery.MAP);
//					trail.mTrailheadsJson = cursor
//							.getString(TrailsQuery.TRAILHEADS);
//					areaName = cursor.getString(TrailsQuery.AREA_NAME);
//
//					MapTrail mapTrail = new MapTrail(context, trail, areaName);
//					mTrails.add(mapTrail);
//
//				}
//
//			} finally {
//				cursor.close();
//			}
//
//		}
//	}

	/**
	 * Load area attributes from the database.
	 * 
	 * @param resolver
	 * @param areaId
	 */
	public void readAreaData(ContentResolver resolver) {
		Cursor cursor = resolver.query(RegionsSchema.buildAreasUri(mRegionId),
				AreasQuery.PROJECTION, null, null, AreasColumns.NAME
						+ " COLLATE LOCALIZED ASC");

		if (cursor == null)
			return;

		try {
			while (cursor.moveToNext()) {
				Area area = new Area();
				area.mId = cursor.getString(AreasQuery.AREA_ID);
				area.mName = cursor.getString(AreasQuery.AREA_NAME);
				double lat0 = cursor.getDouble(AreasQuery.AREA_BBOX_LAT_0);
				double lon0 = cursor.getDouble(AreasQuery.AREA_BBOX_LON_0);
				double lat1 = cursor.getDouble(AreasQuery.AREA_BBOX_LAT_1);
				double lon1 = cursor.getDouble(AreasQuery.AREA_BBOX_LON_1);
				area.setBbox(lon0, lat0, lon1, lat1);
				mAreas.add(area);
			}

		} finally {
			cursor.close();
		}
	}

	/**
	 * {@link org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.AreasSchema} query
	 * parameters.
	 */
	private interface AreasQuery {

		String[] PROJECTION = { AreasColumns.AREA_ID, AreasColumns.NAME,
				AreasColumns.BBOX_LAT_0, AreasColumns.BBOX_LON_0,
				AreasColumns.BBOX_LAT_1, AreasColumns.BBOX_LON_1 };

		int AREA_ID = 0;
		int AREA_NAME = 1;
		int AREA_BBOX_LAT_0 = 2;
		int AREA_BBOX_LON_0 = 3;
		int AREA_BBOX_LAT_1 = 4;
		int AREA_BBOX_LON_1 = 5;
	}

	private interface TrailsQuery {

		String[] PROJECTION = { "trails." + TrailsColumns.TRAIL_ID,
				"trails." + TrailsColumns.NAME,
				"trails." + TrailsColumns.CONDITION,
				"trails." + TrailsColumns.MAP,
				"trails." + TrailsColumns.TRAILHEADS,
				"trails." + TrailsColumns.DIFFICULTY_RATING,
				"trails." + TrailsColumns.AREA_ID, 
				"areas." + AreasColumns.NAME };

		int TRAIL_ID = 0;
		int NAME = 1;
		int CONDITION = 2;
		int MAP = 3;
		int TRAILHEADS = 4;
		int DIFFICULTY_RATING =5;
		@SuppressWarnings("unused")
		int AREA_ID = 6;
		int AREA_NAME = 7;
		
	}

	public void getTrailConditions(Activity activity, String areaId) {
		SmartTrailApplication app = (SmartTrailApplication) activity
				.getApplication();
		ContentResolver resolver = activity.getContentResolver();
		for (MapTrail trail : mTrails) {
			ArrayList<Condition> conditions = null;
			try {

				conditions = app.getApi().getConditionsByTrail(trail.mTrail.mId, 0, 5);

				if (conditions.size() > 0) {
					Condition condition = conditions.get(0);
					trail.setStatus(activity, condition.mStatus, condition.mUpdatedAt);
					trail.mTrail.updateStatus(resolver);
				}

				for (Condition condition : conditions) {
					condition.upsert(resolver);
				}

			} catch (CredentialsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SmartTrailException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	
	/**
	 * Set the width of the trail on the map.
	 * 
	 * @param mTrailId
	 * @param width
	 */
	public void setTrailWidth(String mTrailId, float width) {
		for (MapTrail trail : mTrails) {
			if (trail.mTrail.mId.equals(mTrailId)) {
				trail.mMapPolyline.setWidth(width);
			}
		}

	}
}