/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package org.bouldermountainbike.smarttrail.app;

import static org.bouldermountainbike.smarttrail.app.Config.DEBUG;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.preference.PreferenceManager;

import org.bouldermountainbike.smarttrail.api.SmartTrailApi;
import org.bouldermountainbike.smarttrail.error.CredentialsException;
import org.bouldermountainbike.smarttrail.error.SmartTrailException;
import org.bouldermountainbike.smarttrail.util.AppLog;
import org.bouldermountainbike.smarttrail.util.ImageLoader;
import com.google.android.gms.maps.model.LatLng;

public class SmartTrailApplication extends Application {
	private static final String CLASSTAG = SmartTrailApplication.class
			.getSimpleName();

	public static final String PACKAGE_NAME = "org.bouldermountainbike.smarttrail";
	private static final String PREF_FILE = "/data/data/org.bouldermountainbike.smarttrail/shared_prefs/org.bouldermountainbike.smarttrail.app.prefs.xml";

	static public boolean mZoneEditMode;

	private String mVersion = null;

	public SharedPreferences mPrefs;

	private SmartTrailApi mApi;

	public String mTrail;
	public String mArea;
	public String mLastTabTag = "";

	public int mTrailPosition;

	public boolean mSyncReviews;

	public Typeface mTf;

	public String mUsername;

	public String mEmail;


	public static SmartTrailApplication mInstance;
	private static Context mAppContext;

	// private boolean mIsFirstRun;

	@Override
	public void onCreate() {
		mVersion = getVersionString(this);
		mPrefs =  PreferenceManager
				.getDefaultSharedPreferences(this);

		// delete all the review photos on the sd card.
		ImageLoader loader = new ImageLoader(getApplicationContext());
		loader.clearCache();

		mApi = new SmartTrailApi(SmartTrailApi.createHttpApi(Config.DOMAIN,
				mVersion, false, Config.USE_SSL));

		AppLog.d("loadCredentials()");

		//TODO: get username and password from properties file	
		mApi.setCredentials("USERNAME:", "PASSWORD");

		mUsername = mPrefs.getString(Preferences.PREF_USERNAME, "");
		mEmail= mPrefs.getString(Preferences.PREF_EMAIL, "");
		
		mTrail = Preferences.getTrail(mPrefs);
		mArea = Preferences.getArea(mPrefs);
		mTrailPosition = Preferences.getTrailPosition(mPrefs);

		if (isSignedin()) {
			sendBroadcast(new Intent(Constants.INTENT_ACTION_LOGGED_IN));

		} else {
			sendBroadcast(new Intent(Constants.INTENT_ACTION_LOGGED_OUT));
		}

		mTf = Typeface.createFromAsset(getAssets(),
				"fonts/PFIsotextPro-Bold.otf");

		mInstance = this;
		mAppContext = getApplicationContext();

		AppLog.i("Debug Server:\t" + Config.USE_DEBUG_SERVER);
		AppLog.i("Debug Enabled:\t" + DEBUG);
		AppLog.i("App Version:\t" + mVersion);
	}
	public static SmartTrailApplication getInstance(){
        return mInstance;
    }
	
    public static Context getAppContext() {
        return mAppContext;
    }
    
	public boolean isSignedin() {
		return Preferences.getUsername(mPrefs) != null;
	}

	public SmartTrailApi getApi() {
		return mApi;
	}

	public String getVersion() {

		if (mVersion == null) {
			mVersion = getVersionString(this);
		}
		return mVersion;

	}

	public long getVersionDate() throws ParseException {

		String versionDateStr = getVersionDateString(this);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date versionDate = dateFormat.parse(versionDateStr);
		return versionDate.getTime();
	}


	public void setTrail(String trail, int trailPosition) {
		mTrail = trail;
		mTrailPosition = trailPosition;
		Preferences.storeTrail(mPrefs.edit(), trail);
		Preferences.storeTrailPosition(mPrefs.edit(), trailPosition);
	}

	public void setArea(String area) {
		mArea = area;
		Preferences.storeArea(mPrefs.edit(), area);
	}

	public String getArea() {
		return Preferences.getArea(mPrefs);
	}

	/**
	 * Constructs the version string of the application.
	 * 
	 * @param context
	 *            the context to use for getting package info
	 * @return the versions string of the application
	 */
	private static String getVersionString(Context context) {
		// Get a version string for the app.
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(PACKAGE_NAME, 0);
			return PACKAGE_NAME + ":" + String.valueOf(pi.versionCode);
		} catch (NameNotFoundException e) {
			AppLog.d(CLASSTAG, "Could not retrieve package info");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Constructs the version string of the application.
	 * 
	 * @param context
	 *            the context to use for getting package info
	 * @return the versions string of the application
	 */
	@SuppressWarnings("unused")
	private static String getAppVersion(Context context) {
		PackageManager pm = context.getPackageManager();
		String version = "";
		try {
			// ---get the package info---
			PackageInfo pi = pm.getPackageInfo("com.geozen", 0);

			version = pi.versionName + ":" + Integer.toString(pi.versionCode);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return version;
	}

	/**
	 * Constructs the version date string of the application.
	 * 
	 * @param context
	 *            the context to use for getting package info
	 * @return the versions string of the application
	 */
	private static String getVersionDateString(Context context) {
		// Get a version string for the app.
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(PACKAGE_NAME, 0);
			String[] versionArray = String.valueOf(pi.versionName).split(":");
			return versionArray[0];
		} catch (NameNotFoundException e) {
			AppLog.d(CLASSTAG, "Could not retrieve package info");
			throw new RuntimeException(e);
		}

	}

	public boolean signedInBefore() {
		return true;
	}

	public boolean isFirstRun() {
		File file = new File(PREF_FILE);
		return !file.exists();
	}

	public boolean storeNickname(String nickname) {
		Editor editor = mPrefs.edit();
		Preferences.putNickname(editor, nickname);
		if (!editor.commit()) {
			AppLog.d(CLASSTAG, "storeFirstname commit failed");
			return false;
		}
		return true;
	}

	public CharSequence getNickname() {
		return Preferences.getNickname(mPrefs);
	}

	public CharSequence getUsername() {
		return Preferences.getUsername(mPrefs);
	}

	public CharSequence getPassword() {
		return Preferences.getPassword(mPrefs);
	}

	public void signoutUser() {
//		mApi.clearCredentials();
		Preferences.logoutUser(mPrefs.edit(), true, true);
		Intent intent = new Intent(Constants.INTENT_ACTION_LOGGED_OUT);

		sendBroadcast(intent);

	}

	public boolean signin(String email, String password)
			throws CredentialsException, SmartTrailException, IOException,
			JSONException {

		boolean result;

		mApi.setCredentials(email, password);

		// check if we can login successfully.
		// String appVersion = getAppVersion(this);
		int rc = mApi.signin();

		result = rc >= SmartTrailApi.SIGNIN_SUCCESS;
		if (result) {
			Preferences.storeSignedinAt(mPrefs.edit(), new Date().getTime());
			Preferences.storeUsernamePassword(email, password, mPrefs.edit());
		}
		return result;
	}
	
	public boolean signin(String email)
			throws CredentialsException, SmartTrailException, IOException,
			JSONException {
		
		mEmail = email;
		// check if we can login successfully.
		// String appVersion = getAppVersion(this);
		JSONObject user = mApi.userCheck(email);
		
		mUsername = user.getString("username");
		
		Preferences.storeSignedinAt(mPrefs.edit(), new Date().getTime());
		Preferences.storeUsername(mUsername, mPrefs.edit());
		Preferences.storeEmail(mEmail, mPrefs.edit());
		return true;
	}

	public void setPatrolNumBikers(int numBikers) {
		Preferences.storePatrolNumBikers(mPrefs.edit(), numBikers);
	}

	public int getPatrolNumBikers() {
		return Preferences.getPatrolNumBikers(mPrefs);
	}

	public void setPatrolNumHikers(int num) {
		Preferences.storePatrolNumHikers(mPrefs.edit(), num);
	}

	public int getPatrolNumHikers() {
		return Preferences.getPatrolNumHikers(mPrefs);
	}

	public void setPatrolNumDogsUnleashed(int num) {
		Preferences.storePatrolNumDogsUnleashed(mPrefs.edit(), num);
	}

	public int getPatrolNumDogsUnleashed() {
		return Preferences.getPatrolNumDogsUnleashed(mPrefs);
	}

	public void setPatrolNumDogsLeashed(int num) {
		Preferences.storePatrolNumDogsLeashed(mPrefs.edit(), num);
	}

	public int getPatrolNumDogsLeashed() {
		return Preferences.getPatrolNumDogsLeashed(mPrefs);
	}

	public void setPatrolNumRunners(int num) {
		Preferences.storePatrolNumRunners(mPrefs.edit(), num);
	}

	public int getPatrolNumRunners() {
		return Preferences.getPatrolNumRunners(mPrefs);
	}

	public void setPatrolNumEquestrians(int num) {
		Preferences.storePatrolNumEquestrians(mPrefs.edit(), num);
	}

	public int getPatrolNumEquestrians() {
		return Preferences.getPatrolNumEquestrians(mPrefs);
	}

	public void setPatrolNumAnglers(int num) {
		Preferences.storePatrolNumAnglers(mPrefs.edit(), num);
	}

	public int getPatrolNumAnglers() {
		return Preferences.getPatrolNumAnglers(mPrefs);
	}

	public void setIsPatroller(boolean isPatroller) {
		Preferences.storeIsPatroller(mPrefs.edit(), isPatroller);
	}

	public boolean getIsPatroller() {
		return Preferences.getIsPatroller(mPrefs);
	}

	public String getRegion() {
		return Preferences.getRegion(this, mPrefs);
	}

	public void setRegion(String regionId) {
		Preferences.storeRegion(mPrefs.edit(), regionId);

	}

	public String getRegionName() {
		return Preferences.getRegionName(mPrefs);
	}

	public void setRegionName(String name) {
		Preferences.storeRegionName(mPrefs.edit(), name);
	}
	public LatLng getLastLng() {
		return Preferences.getLastLatLng(mPrefs);
	}
	
	public void setLastLng(LatLng latLng) {
		Preferences.storeLastLatLng(mPrefs.edit(), latLng);
	}



}
