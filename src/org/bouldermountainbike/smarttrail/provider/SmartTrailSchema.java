/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package org.bouldermountainbike.smarttrail.provider;

import java.util.List;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import org.bouldermountainbike.smarttrail.util.ParserUtils;

/**
 * Contract class for interacting with {@link SmartTrailProvider}. Unless
 * otherwise noted, all time-based fields are milliseconds since epoch and can
 * be compared against {@link System#currentTimeMillis()}.
 * <p>
 * The backing {@link android.content.ContentProvider} assumes that {@link Uri}
 * are generated using stronger {@link String} identifiers, instead of
 * {@code int} {@link BaseColumns#_ID} values, which are prone to shuffle during
 * sync.
 */
public class SmartTrailSchema {

	/**
	 * Special value for indicating that an entry has never been updated, or
	 * doesn't exist yet.
	 */
	public static final long UPDATED_NEVER = -2;

	/**
	 * Special value for indicating that the last update time is unknown,
	 * usually when inserted from a local file source.
	 */
	public static final long UPDATED_UNKNOWN = -1;

	public interface AreasColumns {
		/** Unique string identifying the area this trail belongs to. */
		String AREA_ID = "id";

		/** Name of the area. */
		String NAME = "name";

		/** owner of this area (e.g. city,county, us forest..) */
		String OWNER = "owner";

		String URL = "url";

		String NUM_REVIEWS = "numReviews";

		String RATING = "rating";

		/** Body of text explaining this trail in detail. */
		String DESCRIPTION = "description";

		/** User-specific flag indicating starred status. */
		String STARRED = "starred";

		/** color representation of area */
		String COLOR = "color";

		String KEYWORDS = "keywords";

		/** Timestamp of when condition submitted */
		String UPDATED_AT = "updatedAt";

		/** bbox */
		String BBOX_LAT_0 = "bbox_lat_0";
		String BBOX_LON_0 = "bbox_lon_0";
		String BBOX_LAT_1 = "bbox_lat_1";
		String BBOX_LON_1 = "bbox_lon_1";

		String VERSION = "version";
	}

	public interface TrailsColumns {
		/** Unique string identifying this trail. */
		String TRAIL_ID = "id";

		/** Unique string identifying the area this trail belongs to. */
		String AREA_ID = "area_id";

		/**geojson map*/
		String MAP = "map";
		String TRAILHEADS = "trailheads";

		/** Title describing this trail. */
		String NAME = "name";
		/** owner of this trail (e.g. city,county, us forest..) */
		String OWNER = "owner";
		/** owner of this trail (e.g. city,county, us forest..) */
		String URL = "url";
		/** Body of text explaining this trail in detail. */
		String DESCRIPTION = "description";
		
		/** Condition status string */
		String CONDITION = "condition";
		String DIRECTION = "direction";
		String STATUS_UPDATED_AT = "statusUpdatedAt";

		/** User-specific flag indicating starred status. */
		String STARRED = "starred";

		String KEYWORDS = "keywords";

		// Trail head coordinates
		String LENGTH = "length";
		String ELEVATION_GAIN = "elevation_gain";
		String DIFFICULTY_RATING = "difficulty_rating";
		String TECH_RATING = "tech_rating";
		String AEROBIC_RATING = "aerobic_rating";
		String COOL_RATING = "cool_rating";
		
		/** bbox */
		String BBOX_LAT_0 = "bbox_lat_0";
		String BBOX_LON_0 = "bbox_lon_0";
		String BBOX_LAT_1 = "bbox_lat_1";
		String BBOX_LON_1 = "bbox_lon_1";
		
		/** Timestamp of when trail was last updated */
		String UPDATED_AT = "updatedAt";

		String IMAGE_URL = "image_url";
	}

	public interface PoisColumns {

		/** Unique string identifying the region this poi belongs to. */
		String REGION_ID = "regionId";

		/** Unique string identifying the area this poi belongs to. */
		String AREA_ID = "areaId";

		/** Unique string identifying the trail this poi belongs to. */
		String TRAIL_ID = "trailId";
		
		//index into poi array for trail
		String INDEX = "arrayIndex";

		/** Title describing this poi. */
		String NAME = "name";

		/** Body of text explaining this poi in detail. */
		String DESCRIPTION = "description";

		// Location coordinates
		String LAT = "lat";

		String LON = "lon";

	
	}

	public interface ConditionsColumns {
		String CONDITION_ID = "id";
		String TRAIL_ID = "trailId";
		String TYPE = "type";
		String USERNAME = "nickname";
		String USER_TYPE = "userType";
		String CONDITION = "condition";
		String COMMENT = "comment";
		String UPDATED_AT = "updatedAt";
	}

	public interface EventsColumns {
		/** Unique string identifying the area this trail belongs to. */
		String EVENT_ID = "id";

		/** Unique string identifying the region this area belongs to. */
		String REGION_ID = "regionId";

		/** Unique string identifying the region this area belongs to. */
		String ORG_ID = "orgId";

		/** Date/time snippet */
		String SNIPPET = "snippet";

		/** Date/time snippet */
		String DATE_SNIPPET = "dateSnippet";

		/** url */
		String URL = "url";

		/** Timestamp of start */
		String START_TIMESTAMP = "startTimestamp";
		String END_TIMESTAMP = "endTimestamp";

		String UPDATED_AT = "updatedAt";
	}

	public static final String CONTENT_AUTHORITY = "org.bouldermountainbike.smarttrail";

	private static final Uri BASE_CONTENT_URI = Uri.parse("content://"
			+ CONTENT_AUTHORITY);

	// private static final String PATH_AFTER = "after";
	private static final String PATH_EVENTS = "events";
	private static final String PATH_REGIONS = "regions";
	private static final String PATH_AREAS = "areas";
	private static final String PATH_TRAILS = "trails";
	private static final String PATH_TRAILSAREAS = "trailsareas";
	private static final String PATH_CONDITIONS = "conditions";
	private static final String PATH_POIS = "pois";
	private static final String PATH_STARRED = "starred";
	private static final String PATH_SEARCH = "search";
	private static final String PATH_SEARCH_SUGGEST = "search_suggest_query";

	/**
	 * 
	 */
	public static class EventsSchema implements EventsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_EVENTS).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.geozen.smarttrail.event";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.geozen.smarttrail.event";

		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT = START_TIMESTAMP + " ASC";

		/** Build {@link Uri} for requested {@link #EVENT_ID}. */
		public static Uri buildUri(String eventId) {
			return CONTENT_URI.buildUpon().appendPath(eventId).build();
		}

		/** Read {@link #EVENT_ID} from {@link EventsSchema} {@link Uri}. */
		public static String getId(Uri uri) {
			return uri.getPathSegments().get(1);
		}

		//
		// INSERT
		//
		public static String insert(ContentResolver resolver,
				ContentValues values) {
			Uri uri = resolver.insert(CONTENT_URI, values);

			// return the new Id
			return getId(uri);
		}

		//
		// UPDATE
		//
		public static int update(ContentResolver provider, String id,
				ContentValues values) {
			Uri uri = buildUri(id);

			return provider.update(uri, values, null, null);
		}
	}

	/**
	 * Areas are overall categories for {@link TrailsSchema} , such as
	 * "Marshall Mesa" or "Dowdy Draw."
	 */
	public static class RegionsSchema implements AreasColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_REGIONS).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.geozen.smarttrail.region";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.geozen.smarttrail.region";

		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT = NAME + " ASC";

		/**
		 * Build {@link Uri} that references any {@link TrailsSchema} associated
		 * with the requested {@link #AREA_ID}.
		 */
		public static Uri buildAreasUri(String regionId) {
			return CONTENT_URI.buildUpon().appendPath(regionId)
					.appendPath(PATH_AREAS).build();
		}

		public static Uri buildTrailsUri(String regionId) {
			return CONTENT_URI.buildUpon().appendPath(regionId)
					.appendPath(PATH_TRAILSAREAS).build();
		}
		public static Uri buildPoisUri(String regionId) {
			return CONTENT_URI.buildUpon().appendPath(regionId)
					.appendPath(PATH_POIS).build();
		}

		/** Read {@link #AREA_ID} from {@link AreasSchema} {@link Uri}. */
		public static String getRegionId(Uri uri) {
			return uri.getPathSegments().get(1);
		}

	}

	/**
	 * Areas are overall categories for {@link TrailsSchema} , such as
	 * "Marshall Mesa" or "Dowdy Draw."
	 */
	public static class AreasSchema implements AreasColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_AREAS).build();
		public static final Uri CONTENT_STARRED_URI = CONTENT_URI.buildUpon()
				.appendPath(PATH_STARRED).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.geozen.smarttrail.area";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.geozen.smarttrail.area";

		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT = NAME + " ASC";

		/** "All tracks" ID. */
		public static final String ALL_AREAS_ID = "all";

		/** Build {@link Uri} for requested {@link #AREA_ID}. */
		public static Uri buildAreaUri(String areaId) {
			return CONTENT_URI.buildUpon().appendPath(areaId).build();
		}

		/**
		 * Build {@link Uri} that references any {@link TrailsSchema} associated
		 * with the requested {@link #AREA_ID}.
		 */
		public static Uri buildTrailsJoinAreasUri(String areaId) {
			return CONTENT_URI.buildUpon().appendPath(areaId)
					.appendPath(PATH_TRAILSAREAS).build();
		}

		public static Uri buildTrailsUri(String areaId) {
			return CONTENT_URI.buildUpon().appendPath(areaId)
					.appendPath(PATH_TRAILS).build();
		}

		/** Read {@link #AREA_ID} from {@link AreasSchema} {@link Uri}. */
		public static String getAreaId(Uri uri) {
			return uri.getPathSegments().get(1);
		}

		/**
		 * Generate a {@link #AREA_ID} that will always match the requested
		 * {@link Areas} details.
		 */
		public static String generateAreaId(String title) {
			return ParserUtils.sanitizeId(title);
		}

		//
		// INSERT
		//
		public static String insert(ContentResolver resolver,
				ContentValues values) {
			Uri uri = resolver.insert(CONTENT_URI, values);

			// return the new Id
			return getAreaId(uri);
		}

		//
		// UPDATE
		//
		public static int update(ContentResolver provider, String id,
				ContentValues values) {
			Uri uri = buildAreaUri(id);

			return provider.update(uri, values, null, null);
		}
		
		//
		// GET
		//
		public static boolean exists(ContentResolver provider, String id, int version) {
			Uri uri = buildAreaUri(id);
			
			Cursor cursor = provider.query(uri, new String[] { AreasColumns.AREA_ID }, "version >= ?", new String[] {String.valueOf(version)}, null);
			if(cursor.moveToFirst()){
				cursor.close();
			    return true;
			}else{
			    return false;
			}
		}
	}

	/**
	 * Each trail is under an {@link AreasSchema}
	 */
	public static class TrailsSchema implements TrailsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_TRAILS).build();
		public static final Uri CONTENT_STARRED_URI = CONTENT_URI.buildUpon()
				.appendPath(PATH_STARRED).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.geozen.smarttrail.trail";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.geozen.smarttrail.trail";

		public static final String SEARCH_SNIPPET = "search_snippet";

		/** Default "ORDER BY" clause. */
		public static final String DEFAULT_SORT = TrailsColumns.NAME + " ASC";

		/** Build {@link Uri} for requested {@link #TRAIL_ID}. */
		public static Uri buildUri(String trailId) {
			return CONTENT_URI.buildUpon().appendPath(trailId).build();
		}

		/**
		 * Build {@link Uri} that references any {@link AreasSchema} associated
		 * with the requested {@link #TRAIL_ID}.
		 */
		// public static Uri buildAreasDirUri(String trailId) {
		// return CONTENT_URI.buildUpon().appendPath(trailId)
		// .appendPath(PATH_AREAS).build();
		// }

		// public static Uri buildTrailsAtDirUri(long time) {
		// return CONTENT_URI.buildUpon().appendPath(PATH_AT)
		// .appendPath(String.valueOf(time)).build();
		// }
		public static Uri buildConditionsDirUri(String trailId) {
			return CONTENT_URI.buildUpon().appendPath(trailId)
					.appendPath(PATH_CONDITIONS).build();
		}

		public static Uri buildSearchUri(String query) {
			return CONTENT_URI.buildUpon().appendPath(PATH_SEARCH)
					.appendPath(query).build();
		}

		public static boolean isSearchUri(Uri uri) {
			List<String> pathSegments = uri.getPathSegments();
			return pathSegments.size() >= 2
					&& PATH_SEARCH.equals(pathSegments.get(1));
		}

		/** Read {@link #TRAIL_ID} from {@link TrailsSchema} {@link Uri}. */
		public static String getTrailId(Uri uri) {
			return uri.getPathSegments().get(1);
		}

		public static String getSearchQuery(Uri uri) {
			return uri.getPathSegments().get(2);
		}

		/**
		 * Generate a {@link #TRAIL_ID} that will always match the requested
		 * {@link TrailsSchema} details.
		 */
		public static String generateTrailId(String name) {
			return ParserUtils.sanitizeId(name);
		}

		//
		// INSERT
		//
		public static String insert(ContentResolver resolver,
				ContentValues values) {
			Uri uri = resolver.insert(CONTENT_URI, values);

			// return the new Id
			return getTrailId(uri);
		}

		//
		// UPDATE
		//
		public static int update(ContentResolver provider, String id,
				ContentValues values) {
			Uri uri = buildUri(id);

			return provider.update(uri, values, null, null);
		}
	}

	/**
	 * Trail conditions
	 */
	public static class ConditionsSchema implements ConditionsColumns,
			BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_CONDITIONS).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.geozen.smarttrail.condition";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.geozen.smarttrail.condition";

		public static final String DEFAULT_SORT = UPDATED_AT + " DSC";

		//
		// INSERT
		//
		public static String insert(ContentResolver resolver,
				ContentValues values) {
			Uri uri = resolver.insert(CONTENT_URI, values);

			// return the new Id
			return getConditionId(uri);
		}

		//
		// UPDATE
		//
		public static int update(ContentResolver provider, String id,
				ContentValues values) {
			Uri uri = buildUri(id);

			return provider.update(uri, values, null, null);
		}

		public static Uri buildUri(String conditionId) {
			return CONTENT_URI.buildUpon().appendPath(conditionId).build();
		}

		public static String getConditionId(Uri uri) {
			return uri.getPathSegments().get(1);
		}

	}

	/**
	 * Trail Points of Interest (POI)
	 */
	public static class PoisSchema implements PoisColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_POIS).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.geozen.smarttrail.poi";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.geozen.smarttrail.poi";

		public static final String DEFAULT_SORT = NAME + " ASC";

		//
		// INSERT
		//
		public static long insert(ContentResolver resolver,
				ContentValues values) {
			Uri uri = resolver.insert(CONTENT_URI, values);

			// return the new Id
			return getPoiId(uri);
		}

		//
		// UPDATE
		//
		public static int update(ContentResolver provider, long id,
				ContentValues values) {
			Uri uri = buildUri(id);

			return provider.update(uri, values, null, null);
		}

		public static Uri buildUri(long id) {
			return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
		}

		public static long getPoiId(Uri uri) {
			return Long.parseLong(uri.getPathSegments().get(1));
		}

	}

	public static class SearchSuggest {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PATH_SEARCH_SUGGEST).build();

		public static final String DEFAULT_SORT = SearchManager.SUGGEST_COLUMN_TEXT_1
				+ " COLLATE NOCASE ASC";
	}

	private SmartTrailSchema() {
	}
}
