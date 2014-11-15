package org.bouldermountainbike.smarttrail.app;

import java.io.IOException;

import org.bouldermountainbike.smarttrail.R;
import org.bouldermountainbike.smarttrail.error.CredentialsException;
import org.bouldermountainbike.smarttrail.error.SmartTrailException;
import org.bouldermountainbike.smarttrail.ui.MainActivity;
import org.bouldermountainbike.smarttrail.util.AppLog;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;

public class Preferences {
	/**
	 * Singleton to handle preference getting and setting.
	 */
	private static final String CLASSTAG = Preferences.class.getSimpleName();
	public static final String PREFS_NAME = "org.bouldermountainbike.smarttrail.app.prefs";

	// Credentials related preferences
	public static final String PREF_EMAIL = "email";
	public static final String PREF_USERNAME = "username";
	public static final String PREF_PASSWORD = "password";
	public static final String DEFAULT_PASSWORD = "";

	public static final String PREF_TRAIL = "trail";

	public static final String PREF_TRAIL_POSITION = "trailposition";
	public static final int DEFAULT_TRAIL_POSITION = 5;

	private static final String PREF_REGION = "region";
	private static final String PREF_REGION_NAME = "regionName";
	public static final String PREF_AREA = "area";

	public static final String PREF_NICKNAME = "nickname";

	public static final String PREF_SIGNEDIN_AT = "signedinat";

	// Bike Patrol
	private static final String PREF_PATROL_NUM_BIKERS = "patrolNumBikers";
	private static final String PREF_PATROL_NUM_HIKERS = "patrolNumHikers";
	private static final String PREF_PATROL_NUM_RUNNERS = "patrolNumRunners";
	private static final String PREF_PATROL_NUM_DOGS_LEASHED = "patrolNumDogsLeashed";
	private static final String PREF_PATROL_NUM_DOGS_UNLEASHED = "patrolNumDogsUnLeashed";
	private static final String PREF_PATROL_NUM_EQUESTRIANS = "patrolNumEquestrians";
	private static final String PREF_PATROL_NUM_ANGLERS = "patrolNumAnglers";
	private static final String PREF_IS_PATROLLER = "isPatroller";
	private static final String PREF_LAST_LAT = "last_lat";
	private static final String PREF_LAST_LON = "last_lon";

	public static final String PREF_IS_LOCAL_LOADED = "localLoaded";
	public static final String PREF_MAPS_VERSION = "mapsVersion";
	private static final String PREF_DRAWER = "PREF_DRAWER";
	private static final String PREF_AREA_POSITION = "PREF_AREA_POSITION";

	private static SharedPreferences mPrefs;
	private static Preferences mInstance;

	private Preferences(Context context) {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static Preferences getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new Preferences(context);
		}
		return mInstance;

	}

	public int getDrawerPosition() {
		// 0 = Header, 1= Maps, etc...
		return mPrefs.getInt(PREF_DRAWER, MainActivity.DRAWER_MAP_POSITION);
	}

	public boolean saveDrawerPosition(int drawer) {
		if (!mPrefs.edit().putInt(PREF_DRAWER, drawer).commit()) {
			AppLog.d(CLASSTAG, "save drawer position failed");
			return false;
		}
		return true;
	}
	
	public int getAreaPosition() {
		return mPrefs.getInt(PREF_AREA_POSITION, -1);
	}
	
	public boolean saveAreaPosition(int position) {
		if (!mPrefs.edit().putInt(PREF_AREA_POSITION, position).commit()) {
			AppLog.d(CLASSTAG, "save area position failed");
			return false;
		}
		return true;
	}
	
	
	public static CharSequence getNickname(SharedPreferences prefs) {
		return prefs.getString(PREF_NICKNAME, null);
	}

	public static CharSequence getUsername(SharedPreferences prefs) {
		return prefs.getString(PREF_USERNAME, null);
	}

	public static CharSequence getPassword(SharedPreferences prefs) {
		return prefs.getString(PREF_PASSWORD, DEFAULT_PASSWORD);
	}



	public static String getArea(SharedPreferences prefs) {
		return prefs.getString(PREF_AREA, Config.DEFAULT_AREA);
	}

	public static String getTrail(SharedPreferences prefs) {
		return prefs.getString(PREF_TRAIL, Config.DEFAULT_TRAIL);
	}

	public static LatLng getLastLatLng(SharedPreferences prefs) {

		double latitude = (double) prefs.getFloat(PREF_LAST_LAT, 0.0f);
		double longitude = (double) prefs.getFloat(PREF_LAST_LON, 0.0f);

		return new LatLng(latitude, longitude);
	}

	public static boolean storeLastLatLng(Editor editor, LatLng latlng) {
		editor.putFloat(PREF_LAST_LAT, (float) latlng.latitude);
		editor.putFloat(PREF_LAST_LON, (float) latlng.longitude);
		if (!editor.commit()) {
			AppLog.d(CLASSTAG, "store last lat lng commit failed");
			return false;
		}
		return true;
	}

	public static int getTrailPosition(SharedPreferences prefs) {
		return prefs.getInt(PREF_TRAIL_POSITION, DEFAULT_TRAIL_POSITION);
	}

	public static boolean storeUsernamePassword(String username,
			String password, Editor editor) throws CredentialsException,
			SmartTrailException, IOException {
		AppLog.d(CLASSTAG, "Trying to log in.");

		putUsernameAndPassword(editor, username, password);
		if (!editor.commit()) {
			AppLog.d(CLASSTAG, "storeLoginAndPassword commit failed");
			return false;
		}

		return true;
	}

	public static boolean storeUsername(String username, Editor editor) {

		editor.putString(PREF_USERNAME, username);
		if (!editor.commit()) {
			AppLog.d(CLASSTAG, "storeUsername commit failed");
			return false;
		}

		return true;

	}

	public static boolean storeEmail(String email, Editor editor) {

		editor.putString(PREF_EMAIL, email);
		if (!editor.commit()) {
			AppLog.d(CLASSTAG, "storeEmail commit failed");
			return false;
		}

		return true;

	}

	public static boolean logoutUser(Editor editor) {
		return logoutUser(editor, true, true);
	}

	public static boolean logoutUser(Editor editor, boolean clearUsername,
			boolean clearPassword) {
		AppLog.d(CLASSTAG, "Signing out.");

		if (clearUsername) {
			putUsername(editor, null);
		}
		// if (clearPassword) {
		// putPassword(editor, null);
		// }
		if (clearPassword) {
			editor.putString(PREF_EMAIL, null);
		}
		return editor.commit();
	}

	public static void putUsernameAndPassword(final Editor editor,
			String login, String password) {
		editor.putString(PREF_USERNAME, login);
		editor.putString(PREF_PASSWORD, password);
	}

	public static void putUsername(final Editor editor, String username) {
		editor.putString(PREF_USERNAME, username);
	}

	public static void putPassword(final Editor editor, String password) {
		editor.putString(PREF_PASSWORD, password);
	}

	public static void putNickname(final Editor editor, String nickname) {
		editor.putString(PREF_NICKNAME, nickname);
	}

	public static void putTrail(final Editor editor, String trail) {
		editor.putString(PREF_TRAIL, trail);
	}

	public static void putArea(final Editor editor, String area) {
		editor.putString(PREF_AREA, area);
	}

	public static boolean storeArea(Editor editor, String area) {
		editor.putString(PREF_AREA, area);
		if (!editor.commit()) {
			AppLog.d(CLASSTAG, "store area commit failed");
			return false;
		}
		return true;
	}

	public static boolean storeTrail(Editor editor, String trail) {
		editor.putString(PREF_TRAIL, trail);
		if (!editor.commit()) {
			AppLog.d(CLASSTAG, "store trail commit failed");
			return false;
		}
		return true;
	}

	public static boolean storeTrailPosition(Editor editor, int trailPosition) {
		editor.putInt(PREF_TRAIL_POSITION, trailPosition);
		if (!editor.commit()) {
			AppLog.d(CLASSTAG, "store trail commit failed");
			return false;
		}
		return true;
	}

	public static boolean storeSignedinAt(Editor editor, long signedinat) {
		editor.putLong(PREF_SIGNEDIN_AT, signedinat);
		if (!editor.commit()) {
			AppLog.d(CLASSTAG, "store signed in commit failed");
			return false;
		}
		return true;
	}

	public static boolean storePatrolNumBikers(Editor editor, int numBikers) {
		editor.putInt(PREF_PATROL_NUM_BIKERS, numBikers);
		if (!editor.commit()) {
			AppLog.d(CLASSTAG, "store patrol num bikers  commit failed");
			return false;
		}
		return true;

	}

	public static int getPatrolNumBikers(SharedPreferences prefs) {
		return prefs.getInt(PREF_PATROL_NUM_BIKERS, 0);
	}

	public static boolean storePatrolNumHikers(Editor editor, int numHikers) {
		editor.putInt(PREF_PATROL_NUM_HIKERS, numHikers);
		if (!editor.commit()) {
			AppLog.d(CLASSTAG, "store patrol num hikers  commit failed");
			return false;
		}
		return true;

	}

	public static int getPatrolNumHikers(SharedPreferences prefs) {
		return prefs.getInt(PREF_PATROL_NUM_HIKERS, 0);
	}

	public static boolean storePatrolNumDogsUnleashed(Editor editor, int numDogs) {
		editor.putInt(PREF_PATROL_NUM_DOGS_UNLEASHED, numDogs);
		if (!editor.commit()) {
			AppLog.d(CLASSTAG, "store patrol num dogs unleashed  commit failed");
			return false;
		}
		return true;

	}

	public static int getPatrolNumDogsUnleashed(SharedPreferences prefs) {
		return prefs.getInt(PREF_PATROL_NUM_DOGS_UNLEASHED, 0);
	}

	public static boolean storePatrolNumDogsLeashed(Editor editor, int numDogs) {
		editor.putInt(PREF_PATROL_NUM_DOGS_LEASHED, numDogs);
		if (!editor.commit()) {
			AppLog.d(CLASSTAG, "store patrol num dogs leashed  commit failed");
			return false;
		}
		return true;

	}

	public static int getPatrolNumDogsLeashed(SharedPreferences prefs) {
		return prefs.getInt(PREF_PATROL_NUM_DOGS_LEASHED, 0);
	}

	public static boolean storePatrolNumRunners(Editor editor, int numRunners) {
		editor.putInt(PREF_PATROL_NUM_RUNNERS, numRunners);
		if (!editor.commit()) {
			AppLog.d(CLASSTAG, "store patrol num runners  commit failed");
			return false;
		}
		return true;

	}

	public static int getPatrolNumRunners(SharedPreferences prefs) {
		return prefs.getInt(PREF_PATROL_NUM_RUNNERS, 0);
	}

	public static boolean storePatrolNumEquestrians(Editor editor,
			int numEquestrians) {
		editor.putInt(PREF_PATROL_NUM_EQUESTRIANS, numEquestrians);
		if (!editor.commit()) {
			AppLog.d(CLASSTAG, "store patrol num runners  commit failed");
			return false;
		}
		return true;

	}

	public static int getPatrolNumEquestrians(SharedPreferences prefs) {
		return prefs.getInt(PREF_PATROL_NUM_EQUESTRIANS, 0);
	}

	public static boolean storePatrolNumAnglers(Editor editor, int numAnglers) {
		editor.putInt(PREF_PATROL_NUM_ANGLERS, numAnglers);
		if (!editor.commit()) {
			AppLog.d(CLASSTAG, "store patrol num runners  commit failed");
			return false;
		}
		return true;

	}

	public static int getPatrolNumAnglers(SharedPreferences prefs) {
		return prefs.getInt(PREF_PATROL_NUM_ANGLERS, 0);
	}

	public static boolean storeIsPatroller(Editor editor, boolean isPatroller) {
		editor.putBoolean(PREF_IS_PATROLLER, isPatroller);
		if (!editor.commit()) {
			AppLog.d(CLASSTAG, "store patrol num runners  commit failed");
			return false;
		}
		return true;

	}

	public static boolean getIsPatroller(SharedPreferences prefs) {
		return prefs.getBoolean(PREF_IS_PATROLLER, false);
	}

	public static String getRegion(Context context, SharedPreferences prefs) {
		String region = prefs.getString(
				context.getString(R.string.pref_key_region),
				context.getString(R.string.pref_default_region));

		if (region.equals("Boulder County, CO")) {
			return "1";
		} else if (region.equals("Canyon Lands, Utah")) {
			return "2";
		}
		return region;
	}

	public static boolean storeRegion(Editor editor, String regionId) {
		editor.putString(PREF_REGION, regionId);
		if (!editor.commit()) {
			AppLog.d(CLASSTAG, "store region failed");
			return false;
		}
		return true;
	}

	public static String getRegionName(SharedPreferences prefs) {
		return prefs.getString(PREF_REGION_NAME, "Boulder");
	}

	public static boolean storeRegionName(Editor editor, String regionName) {
		editor.putString(PREF_REGION_NAME, regionName);
		if (!editor.commit()) {
			AppLog.d(CLASSTAG, "store region name failed");
			return false;
		}
		return true;
	}



}
