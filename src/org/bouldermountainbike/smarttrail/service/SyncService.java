/**
 * Adapted from code from Google IO scheduler.
 * 
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package org.bouldermountainbike.smarttrail.service;

import java.io.File;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import org.bouldermountainbike.smarttrail.api.SmartTrailApi;
import org.bouldermountainbike.smarttrail.app.SmartTrailApplication;
import org.bouldermountainbike.smarttrail.io.AreasHandler;
import org.bouldermountainbike.smarttrail.io.LocalExecutor;
import org.bouldermountainbike.smarttrail.io.RemoteConditionsExecutor;
import org.bouldermountainbike.smarttrail.io.RemoteStatusesExecutor;
import org.bouldermountainbike.smarttrail.io.RemoteTrailsExecutor;
import org.bouldermountainbike.smarttrail.io.TrailsHandler;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.AreasSchema;
import org.bouldermountainbike.smarttrail.util.AppLog;
import org.bouldermountainbike.smarttrail.util.Util;

/**
 * Background {@link Service} that synchronizes data living in
 * {@link ScheduleProvider}. Reads data from both local {@link Resources} and
 * from remote sources, such as a spreadsheet.
 */
public class SyncService extends IntentService {
	private static final String TAG = "SyncService";

	public static final String EXTRA_STATUS_RECEIVER = "com.google.geozen.smarttrail.extra.STATUS_RECEIVER";
	public static final String INTENT_ACTION_SYNC_COMPLETE = "com.google.geozen.smarttrail.action.SYNC_COMPLETE";

	public static final int STATUS_RUNNING = 0x1;
	public static final int STATUS_ERROR = 0x2;
	public static final int STATUS_FINISHED = 0x3;
	public static final int STATUS_MAPDATA_UPDATED = 0x4;

//	private RemoteConditionsExecutor mRemoteConditionsExecutor;

	// private RemoteEventsExecutor mRemoteEventsExecutor;

	// private RemoteAreasExecutor mRemoteAreasExecutor;

//	private RemoteTrailsExecutor mRemoteTrailsExecutor;

//	private RemoteStatusesExecutor mRemoteStatusExecutor;

	private LocalExecutor mLocalExecutor;

	private AreasHandler mAreasHandler;
	private TrailsHandler mTrailsHandler;

	public SyncService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		final ContentResolver resolver = getContentResolver();

		// mRemoteEventsExecutor = new RemoteEventsExecutor(resolver);
		// mRemoteAreasExecutor = new RemoteAreasExecutor(resolver);
//		mRemoteTrailsExecutor = new RemoteTrailsExecutor(resolver);
//		mRemoteConditionsExecutor = new RemoteConditionsExecutor(resolver);
//		mRemoteStatusExecutor = new RemoteStatusesExecutor(resolver);
		mLocalExecutor = new LocalExecutor(resolver);

		mAreasHandler = new AreasHandler();
		mTrailsHandler = new TrailsHandler();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "onHandleIntent(intent=" + intent.toString() + ")");

		final ResultReceiver receiver = intent
				.getParcelableExtra(EXTRA_STATUS_RECEIVER);
		if (receiver != null)
			receiver.send(STATUS_RUNNING, Bundle.EMPTY);

		SmartTrailApplication app = ((SmartTrailApplication) getApplication());

		final SharedPreferences prefs = getSharedPreferences(
				Prefs.SMARTTRAIL_SYNC, Context.MODE_PRIVATE);
		final long lastUpdate = prefs.getLong(Prefs.LAST_UPDATE, 0);

		//final boolean loadLocal = lastUpdate == 0;
		final boolean loadLocal = true;

		boolean clearedCredentials = false;
		SmartTrailApi api = app.getApi();
		try {
			final long startSync = System.currentTimeMillis();
			final long now = startSync;

			if (loadLocal) {
//				AssetManager aMan = getAssets();
//				String[] regions = aMan.list("smarttrail");
				final ContentResolver resolver = getContentResolver();
				if (!AreasSchema.exists(resolver, "1484", 1)) {
					mLocalExecutor.execute(this, "trails/area_marshall_mesa.json", mAreasHandler);
				}
				mLocalExecutor.execute(this, "trails/area_walker.json", mAreasHandler);
				
				
				mLocalExecutor.execute(this, "trails/trail_walker.json", mTrailsHandler);
				mLocalExecutor.execute(this, "trails/trail_coal_seam.json", mTrailsHandler);
				mLocalExecutor.execute(this, "trails/trail_high_plains.json", mTrailsHandler);
				mLocalExecutor.execute(this, "trails/trail_greenbelt_plateau.json", mTrailsHandler);
				mLocalExecutor.execute(this, "trails/trail_coalton.json", mTrailsHandler);
				
				
				
//				mLocalExecutor.execute(this, "trails/area_whole_enchilada.json", mAreasHandler);
//				mLocalExecutor.execute(this, "trails/area_amasa_back.json", mAreasHandler);
//				mLocalExecutor.execute(this, "trails/area_slickrock.json", mAreasHandler);
//				mLocalExecutor.execute(this, "trails/area_magnificient_7.json", mAreasHandler);
				
//				mLocalExecutor.execute(this, "trails/trail_captain_ahab.json", mTrailsHandler);
//				mLocalExecutor.execute(this, "trails/trail_hymasa.json", mTrailsHandler);
//				
//				mLocalExecutor.execute(this, "trails/trail_slickrock.json", mTrailsHandler);
//				
//				mLocalExecutor.execute(this, "trails/trail_bullrun.json", mTrailsHandler);
//				mLocalExecutor.execute(this, "trails/trail_goldenspike.json", mTrailsHandler);
//				mLocalExecutor.execute(this, "trails/trail_goldbar.json", mTrailsHandler);
//				mLocalExecutor.execute(this, "trails/trail_littlecanyon.json", mTrailsHandler);
//				mLocalExecutor.execute(this, "trails/trail_arthscorner.json", mTrailsHandler);
//				mLocalExecutor.execute(this, "trails/trail_poisonspider.json", mTrailsHandler);
//				mLocalExecutor.execute(this, "trails/trail_gemini.json", mTrailsHandler);
//				
//				
//				mLocalExecutor.execute(this, "trails/trail_whole_enchilada.json", mTrailsHandler);
				prefs.edit().putLong(Prefs.LAST_UPDATE, 1333118577107L)
						.commit();
				AppLog.d(TAG, "local sync took "
						+ (System.currentTimeMillis() - startSync) + "ms");
			} 
//			else {
//				// Always hit remote database for any updates if network
//				// available
//				if (Util.isConnectedToNetwork(this)) {
//
//					long afterTimestamp = lastUpdate;
//					// afterTimestamp = -1;
//
//					int limit = -1;
//					String regionId = app.getRegion();
//
//					if (!api.hasCredentials()) {
//						api.setCredentials("anonymous", "nopassword");
//						clearedCredentials = true;
//					}
//
//					// download new or changed areas
//					// mRemoteAreasExecutor.executeByRegion(api, regionId,
//					// afterTimestamp, limit);
//
//					// download new or changed trails for all areas
//					mRemoteTrailsExecutor.executeByRegion(api, regionId,
//							afterTimestamp, limit);
//
//					// download new or changed statuses for all trails
//					mRemoteStatusExecutor.executeByRegion(api, regionId,
//							afterTimestamp, limit);
//
//					// download new or changed conditions for all trails.
//					// limit 10
//					// per trail
//					mRemoteConditionsExecutor.executeByRegion(api, regionId,
//							afterTimestamp, 10);
//
//					// download new or changed events limit 10 per trail
//					// mRemoteEventsExecutor.executeByRegion(api, regionId, now
//					// - 2 * TimeUtil.DAY_MS, 20);
//
//					// Save last update time
//					prefs.edit().putLong(Prefs.LAST_UPDATE, now).commit();
//
//					Log.d(TAG,
//							"remote sync took "
//									+ (System.currentTimeMillis() - startSync)
//									+ "ms");
//				}
//			}
		} catch (Exception e) {
			AppLog.e(TAG, "Problem while syncing", e);

			if (receiver != null) {
				// Pass back error to surface listener
				final Bundle bundle = new Bundle();
				bundle.putString(Intent.EXTRA_TEXT, e.toString());
				receiver.send(STATUS_ERROR, bundle);
			}
		} finally {
			if (clearedCredentials) {
				api.clearCredentials();
			}

		}

		// Announce success to any surface listener
		AppLog.d(TAG, "sync finished");
		if (receiver != null)
			receiver.send(STATUS_FINISHED, Bundle.EMPTY);

		Intent syncCompleteIntent = new Intent(INTENT_ACTION_SYNC_COMPLETE);
		sendBroadcast(syncCompleteIntent);
	}

	private interface Prefs {
		String LOCAL_LOADED = "localLoaded";
		String LAST_UPDATE = "lastUpdate";
		String SMARTTRAIL_SYNC = "smarttrail_sync";
		// String LOCAL_VERSION = "local_version";
	}
}
