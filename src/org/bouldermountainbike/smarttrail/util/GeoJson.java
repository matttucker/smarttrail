package org.bouldermountainbike.smarttrail.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

public class GeoJson {
	public static LatLng parsePoint(JSONObject geometry) throws JSONException {
		JSONArray coordinates = geometry.getJSONArray("coordinates");
		return parseCoordinate(coordinates);
	};

	public static List<List<LatLng>> parseMultiLineString(JSONObject geometry)
			throws JSONException {
		JSONArray coordinates = geometry.getJSONArray("coordinates");
		List<List<LatLng>> lines = new ArrayList<List<LatLng>>(
				coordinates.length());
		for (int i = 0; i < coordinates.length(); i++) {
			List<LatLng> line = GeoJson.parseLine(coordinates.getJSONArray(i));
			lines.add(line);
		}
		return lines;
	}
	
	public static List<LatLng> parseLine(JSONArray coordinates)
			throws JSONException {

		List<LatLng> line = new ArrayList<LatLng>(coordinates.length());
		for (int i = 0; i < coordinates.length(); i++) {
			LatLng coordinate = parseCoordinate(coordinates.getJSONArray(i));
			line.add(coordinate);
		}
		return line;
	}

	public static LatLng parseCoordinate(JSONArray coordinate)
			throws JSONException {
		double lon = coordinate.getDouble(0);
		double lat = coordinate.getDouble(1);
		return new LatLng(lat, lon);
	}
}
