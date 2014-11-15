package org.bouldermountainbike.smarttrail.io;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;

import org.bouldermountainbike.smarttrail.model.Area;

public class AreasHandler extends JsonHandler {

	@Override
	public void parseAndApply(String json, ContentResolver resolver) throws JSONException {

		JSONObject data = new JSONObject(json);
		Area area = new Area(data);
		area.upsert(resolver);
	}

}
