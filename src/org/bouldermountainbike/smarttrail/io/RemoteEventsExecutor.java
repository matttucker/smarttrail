/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package org.bouldermountainbike.smarttrail.io;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentResolver;
import android.net.Uri;

import org.bouldermountainbike.smarttrail.api.SmartTrailApi;
import org.bouldermountainbike.smarttrail.error.SmartTrailException;
import org.bouldermountainbike.smarttrail.error.CredentialsException;
import org.bouldermountainbike.smarttrail.model.Event;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.EventsSchema;

/**

 */
public class RemoteEventsExecutor {
	private final ContentResolver mResolver;

	public RemoteEventsExecutor(ContentResolver resolver) {
		mResolver = resolver;
	}

	/**
	 * @throws JSONException
	 * @throws SmartTrailException
	 * @throws IOException
	 * @throws CredentialsException
	 * 
	 */
	public void executeByRegion(SmartTrailApi api, String regionId, long afterTimestamp,
			int limit) throws CredentialsException, IOException, SmartTrailException,
			JSONException {
		boolean clearedCredentials = false;

		if (!api.hasCredentials()) {
			api.setCredentials("anonymous", "nopassword");
			clearedCredentials = true;
		}

		JSONArray events = api.pullEventsByRegion(regionId, afterTimestamp,
				limit);

		//remove all previous events from local database.
		Uri uri = EventsSchema.CONTENT_URI;
		mResolver.delete(uri, null, null);
	
		for (int i = 0; i < events.length(); i++) {

			Event event = new Event(events.getJSONObject(i));
			event.upsert(mResolver);
		}

		if (clearedCredentials) {
			api.clearCredentials();
		}
	}

	
}
