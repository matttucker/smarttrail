/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package org.bouldermountainbike.smarttrail.ui;

import java.util.HashMap;

import org.bouldermountainbike.smarttrail.R;
import org.bouldermountainbike.smarttrail.app.SmartTrailApplication;
import org.bouldermountainbike.smarttrail.map.MapTrail;
import org.bouldermountainbike.smarttrail.model.Area;
import org.bouldermountainbike.smarttrail.model.MapData;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.AreasSchema;
import org.bouldermountainbike.smarttrail.service.SyncService;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class TrailMapFragment extends MapFragment implements
		ActionBar.OnNavigationListener, SyncStatusListener {

	static final String EXTRA_TRAIL_ID = "org.bouldermountainbike.smarttrail.extra.TRAIL_ID";
	static final String EXTRA_AREA_ID = "org.bouldermountainbike.smarttrail.extra.AREA_ID";
	public static final String EXTRA_BACK_ENABLED = "back_enabled";
	private static final float SELECTED_TRAIL_WIDTH = 10.0f;
	protected static final float UNSELECTED_TRAIL_WIDTH = 4.0f;
	private static final float AREA_ZOOM_LEVEL = 13.0f;
	// private static TrailMapFragment mInstance;

	GoogleMap mMap;

	private MapData mMapData;

	private HashMap<String, TrailMarker> mMarkerMap;

	private RefreshAreasTask mRefreshAreasTask;
	private RefreshTrailsTask mRefreshTrailsTask;
	private RefreshTrailConditionsTask mRefreshTrailConditionsTask;

	private ArrayAdapter<String> mSpinnerAdapter;
	private SmartTrailApplication mApp;
	private boolean mFirstZoom = true;
	private Menu mOptionsMenu;
	private int mSelectedAreaPosition = -1;
	private boolean mNavigationInitialized;
	private boolean mAreasTaskComplete = false;
	private boolean mTrailsTaskComplete;
	private String mSelectedTrail;
//	private View mRootView;
//	private MapView mMapView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mApp = (SmartTrailApplication) getActivity().getApplication();

		mMarkerMap = new HashMap<String, TrailMarker>();

		setHasOptionsMenu(true);
		mNavigationInitialized = false;

		Bundle args = getArguments();
		if (args != null) {
			mSelectedTrail = args.getString(EXTRA_TRAIL_ID, null);
		}

		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = super.onCreateView(inflater, container, savedInstanceState);
		setUpMapIfNeeded();
		return view;
		// if (mRootView == null) {
		// mRootView = inflater.inflate(R.layout.fragment_map, container,
		// false);
		//
		// mMapView = (MapView) mRootView.findViewById(R.id.mapview);
		// mMapView.onCreate(savedInstanceState);
		//
		// // Gets to GoogleMap from the MapView and does initialization stuff
		// mMap = mMapView.getMap();
		// try {
		// MapsInitializer.initialize(getActivity());
		// } catch (GooglePlayServicesNotAvailableException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// setUpMap();
		// // setUpMapIfNeeded();
		// }
		// return mRootView;
	}

	@Override
	public void onResume() {
		// mMapView.onResume();
		super.onResume();

		boolean refresh = ((ResultHandler) getActivity()).getAndClearRefresh();
		if (refresh) {
			refreshTrailConditions();
		}
	}

	@Override
	public void onPause() {
//		mMapView.onPause();
		super.onPause();

		// We're leaving, so cancel all tasks...
		if (mRefreshAreasTask != null) {
			mRefreshAreasTask.cancel(true);
			mRefreshAreasTask = null;
		}
		if (mRefreshTrailsTask != null) {
			mRefreshTrailsTask.cancel(true);
			mRefreshTrailsTask = null;
		}
		if (mRefreshTrailConditionsTask != null) {
			mRefreshTrailConditionsTask.cancel(true);
			mRefreshTrailConditionsTask = null;
		}

		// ((FragmentChangeActivity) getActivity()).mSyncStatusUpdaterFragment
		// .unregisterListener(this);
	}

	public void refreshTrailConditions() {
		if (mRefreshTrailConditionsTask != null) {
			mRefreshTrailConditionsTask.cancel(true);
		}
		mRefreshTrailConditionsTask = new RefreshTrailConditionsTask();
		mRefreshTrailConditionsTask.execute();
	}

	// @Override
	// public void onDestroy() {
	// super.onDestroy();
	// mMapView.onDestroy();
	// mInstance = null;
	// }
	//
	// @Override
	// public void onLowMemory() {
	// super.onLowMemory();
	// mMapView.onLowMemory();
	// }

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

		mOptionsMenu = menu;
		inflater.inflate(R.menu.map_menu, menu);
		inflater.inflate(R.menu.refresh_menu_items, menu);

		// Start the areas task here (if needed) as we need access to the
		// menu bar.
		if (mAreasTaskComplete) {
			setLoadingAreasState(false);
			if (!mTrailsTaskComplete) {
				mRefreshTrailsTask = new RefreshTrailsTask();
				mRefreshTrailsTask.execute();
			}
		} else {
			mRefreshAreasTask = new RefreshAreasTask();
			mRefreshAreasTask.execute();
		}

	}

	public boolean onOptionsItemSelected(MenuItem item) {

		// Intent intent;
		switch (item.getItemId()) {

		case android.R.id.home:
			if (getFragmentManager().getBackStackEntryCount() != 0) {
				getActivity().onBackPressed();
			}
			return true;

		case R.id.menu_add_condition:

			startAddConditionFragment();
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}

	}

	public void startAddConditionFragment() {
		SmartTrailApplication app = (SmartTrailApplication) getActivity()
				.getApplication();
		if (app.isSignedin()) {
			Fragment frag = new AddConditionFragment();

			Bundle args = new Bundle();
			args.putString(AddConditionFragment.EXTRA_TRAILS_ID, "");
			args.putParcelable(AddConditionFragment.EXTRA_TRAILS_URI,
					AreasSchema.buildTrailsUri(mMapData.mAreas
							.get(mSelectedAreaPosition).mId));
			frag.setArguments(args);

			MainActivity activity = (MainActivity) getActivity();
			activity.switchSubContent(frag, "AddConditionFragment");

		} else {
			Fragment frag = new SigninFragment();

			Bundle args = new Bundle();
			frag.setArguments(args);

			MainActivity activity = (MainActivity) getActivity();
			activity.switchSubContent(frag, "SigninFragment");

		}
	}

	public void animateToCurrentLocation() {
		if (mMap.isMyLocationEnabled()) {
			Location location = mMap.getMyLocation();
			if (location != null) {
				LatLng latlng = new LatLng(location.getLatitude(),
						location.getLongitude());
				mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
			}
		}
	}

	public void setLoading(boolean refreshing) {
		if (mOptionsMenu == null) {
			return;
		}
		final MenuItem addConditionItem = mOptionsMenu
				.findItem(R.id.menu_add_condition);
		if (addConditionItem != null) {
			addConditionItem.setVisible(!refreshing);
		}

		final MenuItem refreshItem = mOptionsMenu.findItem(R.id.menu_refresh);
		if (refreshItem != null) {
			if (refreshing) {
				refreshItem.setVisible(true);
				refreshItem
						.setActionView(R.layout.actionbar_indeterminate_progress);
			} else {
				refreshItem.setActionView(null);
				refreshItem.setVisible(false);

			}
		}
	}

	public void setLoadingAreasState(boolean loading) {

		initAreaNavigationList(loading);
		setLoading(loading);

	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (mNavigationInitialized) {
			mSelectedAreaPosition = itemPosition;

			String newAreaId = mMapData.mAreas.get(itemPosition).mId;
			mApp.setArea(newAreaId);
			draw_trails_and_areas();

			if (mRefreshTrailsTask != null) {
				mRefreshTrailsTask.cancel(true);
				mRefreshTrailsTask = null;
			}
			mRefreshTrailsTask = new RefreshTrailsTask();
			mRefreshTrailsTask.execute();

		} else {
			mNavigationInitialized = true;
		}

		return true;
	}

	/**
	 * Select the area to display.
	 * 
	 * @param id
	 *            String id of the area to display.
	 */
	public void selectAreaById(String id) {

		int position = -1;
		for (int i = 0; i < mMapData.mAreas.size(); i++) {
			if (id.equals(mMapData.mAreas.get(i).mId)) {
				position = i;
			}
		}

		if (position != -1) {
			getActivity().getActionBar().setSelectedNavigationItem(position);
		}

	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			mMap = getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				// The Map is verified. It is now safe to manipulate the map.
				setUpMap();
			}
		}
	}

	private void setUpMap() {

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		String mapType = preferences.getString(
				getString(R.string.pref_key_map_view),
				getString(R.string.pref_default_map_view));

		if (mapType.equalsIgnoreCase("normal")) {
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		} else if (mapType.equalsIgnoreCase("satellite")) {
			mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		} else if (mapType.equalsIgnoreCase("terrain")) {
			mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
		} else if (mapType.equalsIgnoreCase("hybrid")) {
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		}

		mMap.setMyLocationEnabled(true);
		LatLng latLng = mApp.getLastLng();
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,
				AREA_ZOOM_LEVEL));
		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {

				String id = marker.getId();
				TrailMarker tm = mMarkerMap.get(id);

				if (tm == null) {
					return;
				}

				if (tm.mIndex < 0) {

					Fragment fragment = new TrailDetailFragment();
					Bundle args = new Bundle();
					args.putString(TrailDetailFragment.EXTRA_TRAIL_ID,
							tm.mTrailId);
					args.putBoolean(TrailDetailFragment.EXTRA_FROM_MAP, true);
					fragment.setArguments(args);

					MainActivity activity = (MainActivity) getActivity();
					activity.switchSubContent(fragment, "TrailDetailFragment");

				} else {
					getActivity().getActionBar().setSelectedNavigationItem(
							tm.mIndex);
				}

			}

		});
		mMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				String id = marker.getId();
				TrailMarker tm = mMarkerMap.get(id);

				if (tm != null || tm.mIndex < 0) {
					if (mSelectedTrail != null) {
						setTrailWidth(mSelectedTrail, UNSELECTED_TRAIL_WIDTH);
					}

					setTrailWidth(tm.mTrailId, SELECTED_TRAIL_WIDTH);
					mSelectedTrail = tm.mTrailId;
				}

				return false;
			}
		});

		mMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng point) {
				if (mSelectedTrail != null) {
					setTrailWidth(mSelectedTrail, UNSELECTED_TRAIL_WIDTH);
					mSelectedTrail = null;
				}

			}
		});

	}

	private void setTrailWidth(String mTrailId, float width) {
		for (MapTrail trail : mMapData.mTrails) {
			if (trail.mTrail.mId.equals(mTrailId)) {
				trail.mMapPolyline.setWidth(width);
			}
		}

	}

	private void initAreaNavigationList(boolean loading) {
		ActionBar actionBar = getActivity().getActionBar();

		if (loading) {
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			actionBar.setTitle(R.string.app_name);
		} else {
			mNavigationInitialized = false;
			actionBar.setTitle("");
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
			String[] areaNames = new String[mMapData.mAreas.size()];
			for (int i = 0; i < areaNames.length; i++) {
				areaNames[i] = mMapData.mAreas.get(i).mName;
			}

			SmartTrailApplication app = (SmartTrailApplication) getActivity()
					.getApplication();
			mSpinnerAdapter = new ActionBarArrayAdapter(getActivity(),
					R.layout.simple_spinner_dropdown_item, areaNames, app.mTf);

			actionBar.setListNavigationCallbacks(mSpinnerAdapter, this);

			String selectedAreaId = mApp.getArea();
			int i = 0;
			for (Area area : mMapData.mAreas) {
				if (area.mId.equals(selectedAreaId)) {
					mSelectedAreaPosition = i;
					break;
				}
				i++;
			}

			actionBar.setSelectedNavigationItem(mSelectedAreaPosition);
		}
	}

	private void clearMap() {
		mMap.clear();
		mMarkerMap.clear();
	}

	private void addAreasToMap() {
		int index = 0;
		for (Area area : mMapData.mAreas) {
			// mMap.addPolygon(area.mMapBox);

			MarkerOptions mo = new MarkerOptions();
			mo.position(area.mCenter)
					.title(area.mName + " Area")
					// .snippet(area.mName)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.map_area_icon));
			Marker marker = mMap.addMarker(mo);
			TrailMarker tm = new TrailMarker(area.mId, index);
			mMarkerMap.put(marker.getId(), tm);
			index++;

		}

	}

	private void addTrailsToMap() {
		if (mSelectedAreaPosition >= 0
				&& mSelectedAreaPosition < mMapData.mAreas.size()) {

			for (MapTrail trail : mMapData.mTrails) {
				trail.mMapPolyline = mMap.addPolyline(trail.mMapLine);

				if (trail.mTrail.mId.equals(mSelectedTrail)) {
					trail.mMapPolyline.setWidth(SELECTED_TRAIL_WIDTH);
				}
				for (MarkerOptions mo : trail.mTrailheadMarkers) {
					Marker marker = mMap.addMarker(mo);
					mMarkerMap.put(marker.getId(), new TrailMarker(
							trail.mTrail.mId));
				}

				// difficulty marker
				if (trail.mDifficultyMarker != null) {
					Marker marker = mMap.addMarker(trail.mDifficultyMarker);
					mMarkerMap.put(marker.getId(), new TrailMarker(
							trail.mTrail.mId));
				}
			}
		}

	}

	private void animateToAreaCenter(float zoom) {
		if (mSelectedAreaPosition != -1) {
			if (mSelectedAreaPosition > mMapData.mAreas.size()) {
				mSelectedAreaPosition = 0;
			}
			LatLng center = mMapData.mAreas.get(mSelectedAreaPosition).mCenter;
			mApp.setLastLng(center);
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, zoom));
		}
	}

	private void animateToAreaCenter() {
		float zoom = AREA_ZOOM_LEVEL;
		if (!mFirstZoom) {
			zoom = mMap.getCameraPosition().zoom;
			mFirstZoom = false;
		}
		animateToAreaCenter(zoom);
	}

	private void draw_trails_and_areas() {
		animateToAreaCenter(AREA_ZOOM_LEVEL);
		clearMap();
		addAreasToMap();
		addTrailsToMap();

	}

	private void draw_trail_conditions() {
		for (MapTrail trail : mMapData.mTrails) {
			trail.mMapPolyline.setColor(trail.mColor);
		}
	}

	/**
	 * Update areas from the database and redraw.
	 */
	private class RefreshAreasTask extends AsyncTask<Void, Void, Boolean> {

		// private Exception mReason;

		public boolean mComplete;

		@Override
		protected void onPreExecute() {
			mAreasTaskComplete = false;
			setLoadingAreasState(true);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			MapData mapData = new MapData(mApp.getRegion());

			mapData.readAreaData(getActivity().getContentResolver());

			mMapData = mapData;

			return true;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			draw_trails_and_areas();

			setLoadingAreasState(false);

			onNavigationItemSelected(mSelectedAreaPosition, 0);
			mAreasTaskComplete = true;
			mComplete = true;
		}

		@Override
		protected void onCancelled() {
			mComplete = true;
		}
	}

	/**
	 * Update trails from the database and redraw.
	 * 
	 */
	private class RefreshTrailsTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected void onPreExecute() {

			setLoading(true);
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			String areaId = mApp.getArea();
			mMapData.readTrailData(getActivity(), areaId);

			return true;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			draw_trails_and_areas();

			setLoading(false);
			mTrailsTaskComplete = true;
			mRefreshTrailConditionsTask = new RefreshTrailConditionsTask();
			mRefreshTrailConditionsTask.execute();
		}

		@Override
		protected void onCancelled() {
		}
	}

	/**
	 * Update trails from the database and redraw.
	 * 
	 */
	private class RefreshTrailConditionsTask extends
			AsyncTask<Void, Void, Boolean> {

		@Override
		protected void onPreExecute() {

			setLoading(true);
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			String areaId = mApp.getArea();

			mMapData.getTrailConditions(getActivity(), areaId);

			return true;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			draw_trail_conditions();

			setLoading(false);
		}

		@Override
		protected void onCancelled() {
		}
	}

	private class TrailMarker {
		String mTrailId;
		int mIndex;

		public TrailMarker(String trailId, int index) {
			mTrailId = trailId;
			mIndex = index;
		}

		public TrailMarker(String trailId) {
			this(trailId, -1);
		}

	}

	@Override
	public void onStatusChanged(int status) {

		if (status == SyncService.STATUS_MAPDATA_UPDATED) {
			// clearMap();
			// addAreasToMap();
			// addTrailsToMap();

			setLoadingAreasState(false);
		}

		// case SyncService.STATUS_ERROR: {
		// // Error happened down in SyncService, show as toast.
		// mSyncing = false;
		// final String errorText = getString(R.string.toast_sync_error,
		// resultData.getString(Intent.EXTRA_TEXT));
		// Toast.makeText(getActivity(), errorText, Toast.LENGTH_LONG).show();
	};

	class ActionBarArrayAdapter extends ArrayAdapter<String> {

		private Typeface mTf;

		public ActionBarArrayAdapter(Context context, int textViewResourceId,
				String[] items, Typeface tf) {
			super(context, textViewResourceId, items);

			mTf = tf;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			CheckedTextView cv = (CheckedTextView) view
					.findViewById(android.R.id.text1);
			cv.setTypeface(mTf);
			return view;
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {

			View view = super.getDropDownView(position, convertView, parent);
			view.setBackgroundColor(getResources().getColor(R.color.actionbar));
			CheckedTextView cv = (CheckedTextView) view
					.findViewById(android.R.id.text1);
			cv.setTypeface(mTf);
			return view;

		}
	}
}
