/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package org.bouldermountainbike.smarttrail.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.text.TextUtils;

import org.bouldermountainbike.smarttrail.io.GpxSaxHandler;
import org.bouldermountainbike.smarttrail.io.TrailSaxHandler;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.TrailsColumns;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.TrailsSchema;
import org.bouldermountainbike.smarttrail.util.GeoJson;
import com.google.android.gms.maps.model.LatLng;

/**
 * Trail is a specific trail with a trail map. Trails are in trail area and
 * areas are in region.
 * 
 * @author matt
 * 
 */
public class Trail implements BaseColumns, TrailsColumns {

	public static final String JSON_STATUS_UPDATED_AT = "updatedAt";

	public String mId;
	public String mAreaId;

	public String mName;
	public String mOwner;
	public String mUrl;
	public String mDescription;

	public float mLength;
	public float mElevationGain;
	public long mUpdatedAt;
	
	public String mStatus;
	public long mStatusUpdatedAt;

	public int mDifficultyRating;
	public int mTechRating;
	public int mAerobicRating;
	public int mCoolRating;
	
	public String mImageUrl;

	public String mMapJson;
	public String mTrailheadsJson;
	public List<LatLng> mTrailPoints;
	public List<LatLng> mTrailheadPoints;


	/**
	 * Retrieve trail line from a geojson string
	 * 
	 * @param url
	 * @return navigation set
	 * @throws JSONException
	 */
	public static List<LatLng> parseTrail(String json) throws JSONException {

		List<LatLng> data = null;

		if (TextUtils.isEmpty(json))
			return Collections.<LatLng> emptyList();

		JSONObject map = new JSONObject(json);
		JSONArray features = map.getJSONArray("features");

		for (int i = 0; i < features.length(); i++) {
			JSONObject feature = features.getJSONObject(i);
			JSONObject geometry = feature.getJSONObject("geometry");
			String geometryType = geometry.getString("type");
			if (geometryType.equals("MultiLineString")) {
				List<List<LatLng>> lines = GeoJson
						.parseMultiLineString(geometry);
				if (lines.size() > 0) {
					data = lines.get(0);
				}
			}
		}
		if (data == null) {
			return Collections.<LatLng> emptyList();
		} else {
			return data;
		}
	}

	/**
	 * Retrieve traiheads from a geojson string
	 * 
	 * @param url
	 * @return navigation set
	 * @throws JSONException
	 */
	public static List<LatLng> parseTrailheads(String json)
			throws JSONException {

		if (TextUtils.isEmpty(json))
			return Collections.<LatLng> emptyList();

		JSONObject map = new JSONObject(json);
		JSONArray features = map.getJSONArray("features");
		List<LatLng> data = new ArrayList<LatLng>();

		for (int i = 0; i < features.length(); i++) {
			JSONObject feature = features.getJSONObject(i);
			// String type = feature.getString("type");
			JSONObject geometry = feature.getJSONObject("geometry");
			String geometryType = geometry.getString("type");
			if (geometryType.equals("Point")) {
				LatLng point = GeoJson.parsePoint(geometry);
				data.add(point);
			}
		}
		return data;
	}

	/**
	 * Retrieve trail data set from a gpx input source
	 * 
	 * @param url
	 * @return navigation set
	 */
	public static List<LatLng> parseGpx(InputSource is)
			throws ParserConfigurationException, SAXException, IOException {

		// Get a SAXParser from the SAXPArserFactory.
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();

		// Get the XMLReader of the SAXParser we created.
		XMLReader xr = sp.getXMLReader();

		// Create a new ContentHandler and apply it to the XML-Reader
		GpxSaxHandler trailSaxHandler = new GpxSaxHandler();
		xr.setContentHandler(trailSaxHandler);

		// Parse the xml-data from our source
		xr.parse(is);

		// Our TrailSaxHandler now provides the parsed data to us.
		return trailSaxHandler.getPoints();
	}

	/**
	 * Retrieve trail data set from an input source
	 * 
	 * @param url
	 * @return navigation set
	 */
	public static List<LatLng> parseKml(InputSource is)
			throws ParserConfigurationException, SAXException, IOException {

		// Get a SAXParser from the SAXPArserFactory.
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();

		// Get the XMLReader of the SAXParser we created.
		XMLReader xr = sp.getXMLReader();

		// Create a new ContentHandler and apply it to the XML-Reader
		TrailSaxHandler trailSaxHandler = new TrailSaxHandler();
		xr.setContentHandler(trailSaxHandler);

		// Parse the xml-data from our source
		xr.parse(is);

		// Our TrailSaxHandler now provides the parsed data to us.
		return trailSaxHandler.mPoints;

	}

	public Trail() {
	}

	public Trail(JSONObject trail) throws JSONException {
		mId = trail.getString(TRAIL_ID);
		mAreaId = trail.getString(AREA_ID);

		mName = trail.optString(NAME, "");

		mMapJson = trail.optString(MAP, "");
		mTrailheadsJson = trail.optString(TRAILHEADS, "");

		mOwner = trail.optString(OWNER, "");
		mUrl = trail.optString(URL, "");
		mDescription = trail.optString(DESCRIPTION, "");
		mUpdatedAt = trail.optLong(UPDATED_AT, 0);

		mLength = (float) trail.optDouble(LENGTH, 0.0f);
		mElevationGain = (float) trail.optDouble(ELEVATION_GAIN, 0.0f);

		mDifficultyRating = trail.optInt(DIFFICULTY_RATING, 1);
		mTechRating = trail.optInt(TECH_RATING, 1);
		mAerobicRating = trail.optInt(AEROBIC_RATING, 1);
		mCoolRating = trail.optInt(COOL_RATING, 1);
		
		mImageUrl = trail.optString(IMAGE_URL,"");

	}

	public ContentValues getValues() {
		ContentValues values = new ContentValues();

		values.put(TRAIL_ID, mId);
		values.put(AREA_ID, mAreaId);
		values.put(MAP, mMapJson);
		values.put(TRAILHEADS, mTrailheadsJson);
		values.put(NAME, mName);
		values.put(OWNER, mOwner);
		values.put(DESCRIPTION, mDescription);
		values.put(URL, mUrl);
		values.put(LENGTH, mLength);
		values.put(ELEVATION_GAIN, mElevationGain);
		values.put(DIFFICULTY_RATING, mDifficultyRating);
		values.put(TECH_RATING, mTechRating);
		values.put(AEROBIC_RATING, mAerobicRating);
		values.put(COOL_RATING, mCoolRating);
		values.put(IMAGE_URL, mImageUrl);
		
		values.put(CONDITION, mStatus);
		values.put(STATUS_UPDATED_AT, mStatusUpdatedAt);

		return values;
	}

	public String insert(ContentResolver provider) {
		String id = TrailsSchema.insert(provider, getValues());
		mId = id;
		return id;
	}

	public int update(ContentResolver provider) {
		return TrailsSchema.update(provider, mId, getValues());
	}
	public int updateStatus(ContentResolver provider) {
		ContentValues values = new ContentValues();
		values.put(CONDITION, mStatus);
		values.put(STATUS_UPDATED_AT, mStatusUpdatedAt);
		return TrailsSchema.update(provider, mId, values);
	}
	

	public String upsert(ContentResolver provider) {
		Cursor cursor = provider.query(TrailsSchema.CONTENT_URI,
				new String[] { TrailsColumns.TRAIL_ID }, TrailsColumns.TRAIL_ID
						+ "='" + mId + "'", null, null);
		if (cursor == null || cursor.getCount() == 0) {
			return insert(provider);
		} else {
			update(provider);
			return mId;
		}
	}

//	public void inflateTrailheads(String areaName, String jsonTrailheads)
//			throws JSONException {
//		mTrailheadPoints = parseTrailheads(jsonTrailheads);
//
//		for (LatLng point : mTrailheadPoints) {
//			MarkerOptions marker = new MarkerOptions();
//			marker.position(point)
//					.title(mName)
//					.snippet(areaName)
//					.icon(BitmapDescriptorFactory
//							.fromResource(R.drawable.map_icon));
//			mTrailheadMarkers.add(marker);
//		}
//
//	}

//	public void inflateTrail(Context context, String jsonTrail)
//			throws JSONException {
//		mTrailPoints = parseTrail(jsonTrail);
//		mMapLine.width(4.0f).addAll(mTrailPoints)
//				.color(Condition.getStatusColor(context, mStatus));
//	}

//	public void setStatus(Context context, String status) {
//		mStatus = status;
//		mMapLine.color(Condition.getStatusColor(context, mStatus));
//
//	}

	public List<LatLng> getTrailPoints() throws JSONException {
		mTrailPoints = parseTrail(mMapJson);
		return mTrailPoints;
	}
	

	public List<LatLng> getTrailHeadPoints() throws JSONException {
		mTrailheadPoints = parseTrailheads(mTrailheadsJson);
		return mTrailheadPoints;
	}
}

// public List<LatLng> getMapPoints() throws JSONException {
// return parseTrail(mMap);
// }
//
// public List<LatLng> getTrailheads() throws JSONException {
//
// return parseTrailheads(mTrailheads);
// }
//
// /**
// * Read trail points from storage.
// *
// * @param context
// * @param regionId
// * @param mapId
// * @return
// * @throws IOException
// * @throws ParserConfigurationException
// * @throws SAXException
// */
// public static List<LatLng> readMap(Context context, String regionId,
// String mapId) throws IOException, ParserConfigurationException,
// SAXException {
//
// if (TextUtils.isEmpty(regionId) || TextUtils.isEmpty(mapId))
// return Collections.emptyList();
//
// File dir = Environment.getExternalStorageDirectory();
// String fileName = Constants.CACHE_DIR + "/" + regionId + "/" + mapId
// + ".gpx";
// File mapFile = new File(dir, fileName);
//
// List<LatLng> data = Collections.emptyList();
//
// if (mapFile.exists()) {
// data = readFromCache(mapFile);
// } else {
// data = readFromAssets(context, fileName);
// }
//
// return data;
//
// }
//
// /**
// * Read trail points form file in assets.
// *
// * @param context
// * @param fileName
// * @return
// * @throws IOException
// * @throws ParserConfigurationException
// * @throws SAXException
// */
// public static List<LatLng> readFromAssets(Context context, String fileName)
// throws IOException, ParserConfigurationException, SAXException {
//
// InputStream is = context.getAssets().open(fileName);
//
// if (is == null)
// return null;
//
// try {
// InputSource source = new InputSource(is);
// List<LatLng> data = parseGpx(source);
// return data;
// } finally {
// is.close();
// }
// }
//
// /**
// * Read trail points form file in cache.
// *
// * @param file
// * @return
// * @throws IOException
// * @throws ParserConfigurationException
// * @throws SAXException
// */
// public static List<LatLng> readFromCache(File file) throws IOException,
// ParserConfigurationException, SAXException {
//
// if (!file.exists())
// return null;
//
// FileInputStream is = new FileInputStream(file);
//
// try {
// InputSource source = new InputSource(is);
// List<LatLng> data = parseGpx(source);
// return data;
// } finally {
// is.close();
// }
//
// }