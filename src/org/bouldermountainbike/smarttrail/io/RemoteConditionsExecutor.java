/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package org.bouldermountainbike.smarttrail.io;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentResolver;

import org.bouldermountainbike.smarttrail.api.SmartTrailApi;
import org.bouldermountainbike.smarttrail.error.SmartTrailException;
import org.bouldermountainbike.smarttrail.error.CredentialsException;
import org.bouldermountainbike.smarttrail.model.Condition;

/**

 */
public class RemoteConditionsExecutor {
	private final ContentResolver mResolver;

	public RemoteConditionsExecutor(ContentResolver resolver) {
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
			int limit) throws CredentialsException,
			IOException, SmartTrailException, JSONException {
		boolean clearedCredentials = false;

		if ( !api.hasCredentials()) {
			api.setCredentials("anonymous", "nopassword");
			clearedCredentials = true;
		}

		JSONArray conditions = api.pullConditionsByRegion(regionId, afterTimestamp,
				limit);


		for (int i = 0; i < conditions.length(); i++) {

			Condition condition = new Condition(conditions.getJSONObject(i));
			
			Condition.purgeConditionsToLimit(mResolver, condition.mTrailId, 9);
			
			condition.upsert(mResolver);
		}

		if (clearedCredentials) {
			api.clearCredentials();
		}
	}

	

}
