package org.bouldermountainbike.smarttrail.io;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;

import org.bouldermountainbike.smarttrail.model.Trail;

public class TrailsHandler extends JsonHandler {

	@Override
	public void parseAndApply(String json, ContentResolver resolver)
			throws JSONException {

		JSONObject data = new JSONObject(json);
		Trail trail = new Trail(data);
		trail.upsert(resolver);
		
	}

}
