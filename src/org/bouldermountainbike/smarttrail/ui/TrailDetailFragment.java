/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package org.bouldermountainbike.smarttrail.ui;

import java.io.IOException;
import java.util.ArrayList;

import org.bouldermountainbike.smarttrail.R;
import org.bouldermountainbike.smarttrail.app.Constants;
import org.bouldermountainbike.smarttrail.app.SmartTrailApplication;
import org.bouldermountainbike.smarttrail.app.VolleySingleton;
import org.bouldermountainbike.smarttrail.error.CredentialsException;
import org.bouldermountainbike.smarttrail.error.SmartTrailException;
import org.bouldermountainbike.smarttrail.map.MapTrail;
import org.bouldermountainbike.smarttrail.model.Condition;
import org.bouldermountainbike.smarttrail.model.MapData;
import org.bouldermountainbike.smarttrail.model.Trail;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.AreasSchema;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.ConditionsColumns;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.TrailsColumns;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.TrailsSchema;
import org.bouldermountainbike.smarttrail.util.FadeInNetworkImageView;
import org.bouldermountainbike.smarttrail.util.NotifyingAsyncQueryHandler;
import org.bouldermountainbike.smarttrail.util.UnitsUtil;
import org.json.JSONException;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * A fragment that shows detail information for a trail, including trail title,
 * length, elevation gain, ratings, description, directions, etc.
 */
public class TrailDetailFragment extends Fragment implements
		NotifyingAsyncQueryHandler.AsyncQueryListener,
		CompoundButton.OnCheckedChangeListener {

	/**
	 * Since trails belong to areas, the parent activity can send this extra
	 * specifying an area URI that should be used for coloring the title-bar.
	 */
	public static final String EXTRA_AREA = "org.bouldermountainbike.smarttrail.extra.AREA";
	static final String EXTRA_TRAIL_ID = "trail_id";
	static final String EXTRA_FROM_MAP = "org.bouldermountainbike.smarttrail.extra.FROM_MAP";

	private String mAreaId;
	private String mTrailId;

	private Uri mTrailUri;

	private View mRootView;

	private NotifyingAsyncQueryHandler mHandler;

	private EllipsizingTextView mDescriptionTextView;

	private RatingBar mTechRating;

	private RatingBar mAerobicRating;

	private RatingBar mCoolRating;

	private LinearLayout mOverviewContainer;

	private ImageLoader mImageLoader;
	private FadeInNetworkImageView mBannerImage;
	private ImageView mDifficultyRating;
	private TextView mLengthTextView;
	private TextView mGainTextView;
	private ImageButton mDirectionsButton;
	private ImageButton mConditionsButton;
	private GoogleMap mMap;
	private MapView mMapView;

	private MapData mMapData;
	private RefreshTrailsTask mRefreshTrailsTask;
	private GetTrailConditionTask mGetTrailConditionTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			mTrailId = args.getString(EXTRA_TRAIL_ID);
			mTrailUri = TrailsSchema.buildUri(mTrailId);
		}

		setHasOptionsMenu(true);

		// update the actionbar to show the up carat/affordance
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		mImageLoader = VolleySingleton.getInstance().getImageLoader();
		mMapData = new MapData("1");

		// setRetainInstance(true);
	}

	@Override
	public void onResume() {
		mMapView.onResume();
		super.onResume();

	}

	@Override
	public void onPause() {
		if (mRefreshTrailsTask != null) {
			mRefreshTrailsTask.cancel(true);
		}
		if (mGetTrailConditionTask != null) {
			mGetTrailConditionTask.cancel(true);
		}

		mMapView.onPause();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mMapView.onLowMemory();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (mTrailUri == null) {
			return;
		}

		// Start background queries to load trail/area/conditions details
		final Uri conditionsUri = TrailsSchema.buildConditionsDirUri(mTrailId);

		mHandler = new NotifyingAsyncQueryHandler(getActivity()
				.getContentResolver(), this);
		mHandler.startQuery(TrailsQuery._TOKEN, mTrailUri,
				TrailsQuery.PROJECTION);

		// Date now = new Date();
		// String selection = ConditionsColumns.UPDATED_AT + " > "
		// + Long.toString(now.getTime() - 5 * TimeUtil.DAY_MS);

		String selection = "";
		mHandler.startQuery(ConditionsQuery._TOKEN, null, conditionsUri,
				ConditionsQuery.PROJECTION, selection, null,
				ConditionsColumns.UPDATED_AT + " DESC");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

		SmartTrailApplication app = (SmartTrailApplication) getActivity()
				.getApplication();

		mRootView = inflater.inflate(R.layout.fragment_trail_detail, container,
				false);

		mMapView = (MapView) mRootView.findViewById(R.id.mapview);
		mMapView.onCreate(savedInstanceState);

		// Gets to GoogleMap from the MapView and does initialization stuff
		mMap = mMapView.getMap();
//		try {
			MapsInitializer.initialize(getActivity());
//		} catch (GooglePlayServicesNotAvailableException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		setUpMap();

		mBannerImage = (FadeInNetworkImageView) mRootView
				.findViewById(R.id.bannerImage);
		// mBannerImage.setDefaultImageResId(R.drawable.default_trail_image);
		mBannerImage.setErrorImageResId(R.drawable.default_trail_image);

		mDifficultyRating = (ImageView) mRootView
				.findViewById(R.id.difficultyRating);

		TextView descriptionTitleTextView = (TextView) mRootView
				.findViewById(R.id.descripTitle);
		descriptionTitleTextView.setTypeface(app.mTf);

		mDescriptionTextView = (EllipsizingTextView) mRootView
				.findViewById(R.id.overview);
		mLengthTextView = (TextView) mRootView.findViewById(R.id.length);
		mGainTextView = (TextView) mRootView.findViewById(R.id.gain);

		RelativeLayout infoView = (RelativeLayout) mRootView
				.findViewById(R.id.info);

		mTechRating = (RatingBar) infoView.findViewById(R.id.technicalRating);
		mAerobicRating = (RatingBar) infoView.findViewById(R.id.aerobicRating);
		mCoolRating = (RatingBar) infoView.findViewById(R.id.coolRating);

		mOverviewContainer = (LinearLayout) mRootView
				.findViewById(R.id.overviewContainer);

		mConditionsButton = (ImageButton) mRootView
				.findViewById(R.id.conditions);
		mDirectionsButton = (ImageButton) mRootView
				.findViewById(R.id.directions);

		mGetTrailConditionTask = new GetTrailConditionTask();
		mGetTrailConditionTask.execute();

		return mRootView;
	}

	/**
	 * {@inheritDoc}
	 */
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		if (getActivity() == null) {
			return;
		}

		if (token == TrailsQuery._TOKEN) {
			onTrailQueryComplete(cursor);
		} else if (token == ConditionsQuery._TOKEN) {
			onConditionsQueryComplete(cursor);
		} else {
			cursor.close();
		}
	}

	/**
	 * Handle {@link TrailsQuery} {@link Cursor}.
	 */
	@SuppressWarnings("unused")
	private void onTrailQueryComplete(Cursor cursor) {
		try {
			if (!cursor.moveToFirst()) {
				return;
			}
			final Trail trail = new Trail();

			trail.mName = cursor.getString(TrailsQuery.NAME);
			trail.mAreaId = cursor.getString(TrailsQuery.AREA_ID);
			trail.mTrailheadsJson = cursor.getString(TrailsQuery.TRAILHEADS);
			trail.mImageUrl = cursor.getString(TrailsQuery.IMAGE_URL);

			final String trailName = cursor.getString(TrailsQuery.NAME);
			getActivity().getActionBar().setTitle(trailName);
			final int titleId = Resources.getSystem().getIdentifier(
					"action_bar_title", "id", "android");
			TextView title = (TextView) getActivity().getWindow().findViewById(
					titleId);
			SmartTrailApplication app = (SmartTrailApplication) getActivity()
					.getApplication();
			title.setTypeface(app.mTf);
			mAreaId = cursor.getString(TrailsQuery.AREA_ID);
			app.setArea(mAreaId);

			int lengthMeters = cursor.getInt(TrailsQuery.LENGTH);
			int gainMeters = cursor.getInt(TrailsQuery.ELEVATION_GAIN);

			float length = 0.0f;
			String lengthUnits = "miles";
			float gain = 0.0f;
			String gainUnits = "ft";
			if (false) {
				// if (lengthMeters > 1000) {
				// mSubtitle.setText(((float) lengthMeters / 1000)
				// + " km    gain: " + gainMeters + " m");
				// } else {
				// mSubtitle.setText(lengthMeters + " m    gain: "
				// + gainMeters + " m");
				// }
			} else {
				length = UnitsUtil.metersToMiles(lengthMeters);
				gain = UnitsUtil.metersToFeet(gainMeters);
			}
			Resources res = getResources();
			String lengthText = String.format(res.getString(R.string.length),
					length, lengthUnits);
			String gainText = String.format(res.getString(R.string.gain), gain,
					gainUnits);
			mLengthTextView.setText(lengthText);
			mGainTextView.setText(gainText);

			trail.mDifficultyRating = cursor
					.getInt(TrailsQuery.DIFFICULTY_RATING);

			float techRating = cursor.getInt(TrailsQuery.TECHNICAL_RATING);
			float aerobicRating = cursor.getInt(TrailsQuery.AEROBIC_RATING);
			float coolRating = cursor.getInt(TrailsQuery.COOL_RATING);

			mBannerImage.setImageUrl(trail.mImageUrl, mImageLoader);

			switch (trail.mDifficultyRating) {
			case 5:
				mDifficultyRating
						.setImageResource(R.drawable.imba_extremely_difficult);
				break;

			case 4:
				mDifficultyRating
						.setImageResource(R.drawable.imba_very_difficult);
				break;

			case 3:
				mDifficultyRating
						.setImageResource(R.drawable.imba_more_difficult);
				break;

			case 2:
				mDifficultyRating.setImageResource(R.drawable.imba_easy);
				break;

			case 1:
				mDifficultyRating.setImageResource(R.drawable.imba_easiest);
				break;

			}
			mDifficultyRating.setVisibility(View.VISIBLE);
			mTechRating.setRating(techRating);
			mAerobicRating.setRating(aerobicRating);
			mCoolRating.setRating(coolRating);

			// Unregister around setting checked state to avoid triggering
			// listener since change isn't user generated.
			// mStarred.setOnCheckedChangeListener(null);

			int intval = cursor.getInt(TrailsQuery.STARRED);
			// boolean val = !(intval == 0);
			// // boolean val = (intval != 0); huh? doesn't work
			// mStarred.setChecked(val);
			// mStarred.setOnCheckedChangeListener(this);

			final String description = cursor
					.getString(TrailsQuery.DESCRIPTION);

			if (TextUtils.isEmpty(description)) {
				mDescriptionTextView.setText(R.string.noDescription);
				mOverviewContainer.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

					}
				});
			} else {

				mDescriptionTextView.setMaxLines(4);
				mDescriptionTextView.setText(Html.fromHtml(description));

				mOverviewContainer.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Fragment frag = new TrailDescriptionFragment();

						Bundle args = new Bundle();
						args.putString(TrailDescriptionFragment.EXTRA_TRAIL_ID,
								mTrailId);
						args.putString(
								TrailDescriptionFragment.EXTRA_TRAIL_NAME,
								trailName);
						args.putString(
								TrailDescriptionFragment.EXTRA_TRAIL_OVERVIEW,
								description);

						frag.setArguments(args);

						FragmentTransaction transaction = getActivity()
								.getFragmentManager().beginTransaction();

						// Replace whatever is in the fragment_container view
						// with this fragment, and add the transaction to the
						// back stack
						transaction.replace(R.id.content_frame, frag);
						transaction.addToBackStack(null);

						// Commit the transaction
						transaction.commit();

					}
				});
			}

			mConditionsButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Fragment frag = new ConditionsFragment();

					Bundle args = new Bundle();
					args.putString(TrailDescriptionFragment.EXTRA_TRAIL_ID,
							mTrailId);
					args.putString(Constants.EXTRA_AREA_ID, mAreaId);

					frag.setArguments(args);

					MainActivity activity = (MainActivity) getActivity();
					activity.switchSubContent(frag, "ConditionsFragment");

				}
			});

			try {
				trail.getTrailHeadPoints();
				if (trail.mTrailheadPoints.size() > 0) {
					mDirectionsButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {

							LatLng trailhead = trail.mTrailheadPoints.get(0);

							String url = "http://maps.google.com/maps?daddr="
									+ Double.toString(trailhead.latitude) + ","
									+ Double.toString(trailhead.longitude);
							Intent intent = new Intent(
									android.content.Intent.ACTION_VIEW, Uri
											.parse(url));
							startActivity(intent);

						}
					});

					mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
							trail.mTrailheadPoints.get(0), 13.0f));
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (mRefreshTrailsTask != null) {
				mRefreshTrailsTask.cancel(true);
			}
			mRefreshTrailsTask = new RefreshTrailsTask();
			mRefreshTrailsTask.execute();

		} finally {
			cursor.close();
		}
	}

	/**
	 * Handle {@link TrailsQuery} {@link Cursor}.
	 */
	private void onConditionsQueryComplete(Cursor cursor) {
		// mConditionsCursor = cursor;
		// getActivity().startManagingCursor(mConditionsCursor);
		// mConditionsAdapter.changeCursor(mConditionsCursor);
		try {
			if (!cursor.moveToFirst()) {
				return;
			}

			String status = cursor.getString(ConditionsQuery.STATUS);
			setTrailStatus(status);

		} finally {
			cursor.close();
		}

	}

	void setTrailStatus(String status) {
		if (mConditionsButton == null) {
			return;
		}

		if (status.equalsIgnoreCase("good")) {
			mConditionsButton.setImageResource(R.drawable.meter_good);
		} else if (status.equalsIgnoreCase("fair")) {
			mConditionsButton.setImageResource(R.drawable.meter_fair);
		} else if (status.equalsIgnoreCase("poor")) {
			mConditionsButton.setImageResource(R.drawable.meter_poor);
		} else if (status.equalsIgnoreCase("closed")) {
			mConditionsButton.setImageResource(R.drawable.meter_closed);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.trail_detail_menu_items, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// final String shareString;

		switch (item.getItemId()) {

		case android.R.id.home:
			getActivity().onBackPressed();
			return true;

		case R.id.menu_add_condition:

			startAddConditionFragment(R.id.content_frame);

			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	public void startAddConditionFragment(int containerId) {
		final Intent intent;
		SmartTrailApplication app = (SmartTrailApplication) getActivity()
				.getApplication();
		if (app.isSignedin()) {
			Fragment frag = new AddConditionFragment();

			Bundle args = new Bundle();
			args.putString(AddConditionFragment.EXTRA_TRAILS_ID, mTrailId);
			args.putParcelable(AddConditionFragment.EXTRA_TRAILS_URI,
					AreasSchema.buildTrailsUri(mAreaId));
			frag.setArguments(args);

			MainActivity activity = (MainActivity) getActivity();
			activity.switchSubContent(frag, "AddConditionFragment");

		} else {
			Fragment frag = new SigninFragment();

			MainActivity activity = (MainActivity) getActivity();
			activity.switchSubContent(frag, "SigninFragment");

		}
	}

	/**
	 * Handle toggling of starred checkbox.
	 */
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		final ContentValues values = new ContentValues();
		values.put(TrailsSchema.STARRED, isChecked ? 1 : 0);
		mHandler.startUpdate(mTrailUri, values);

		// Because change listener is set to null during initialization, these
		// won't fire on pageview.
	}

	/**
	 * {@link org.bouldermountainbike.smarttrail.provider.TrailsSchema} query
	 * parameters.
	 */
	private interface TrailsQuery {
		int _TOKEN = 0x1;

		String[] PROJECTION = {

		TrailsColumns.NAME, TrailsColumns.STARRED, TrailsColumns.DESCRIPTION,
				TrailsColumns.URL, TrailsColumns.OWNER, TrailsColumns.LENGTH,
				TrailsColumns.ELEVATION_GAIN, TrailsColumns.DIFFICULTY_RATING,
				TrailsColumns.TECH_RATING, TrailsColumns.AEROBIC_RATING,
				TrailsColumns.COOL_RATING, TrailsColumns.TRAILHEADS,
				TrailsColumns.AREA_ID, TrailsColumns.IMAGE_URL };

		int NAME = 0;
		int STARRED = 1;
		int DESCRIPTION = 2;
		@SuppressWarnings("unused")
		int URL = 3;
		@SuppressWarnings("unused")
		int OWNER = 4;
		int LENGTH = 5;
		int ELEVATION_GAIN = 6;
		int DIFFICULTY_RATING = 7;
		int TECHNICAL_RATING = 8;
		int AEROBIC_RATING = 9;
		int COOL_RATING = 10;
		int TRAILHEADS = 11;
		int AREA_ID = 12;
		int IMAGE_URL = 13;

	}

	private interface ConditionsQuery extends ConditionsAdapter.ConditionsQuery {
		int _TOKEN = 0x3;

		String[] PROJECTION = { ConditionsColumns.CONDITION };
		int STATUS = 0;
	}

	/*
	 * 
	 * 
	 */
	private class GetTrailConditionTask extends AsyncTask<Void, Void, Boolean> {

		private Condition mLastCondition;

		// private Exception mReason;

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected Boolean doInBackground(Void... params) {

			ContentResolver resolver = getActivity().getContentResolver();
			SmartTrailApplication app = (SmartTrailApplication) getActivity()
					.getApplication();
			try {

				ArrayList<Condition> conditions = app.getApi()
						.getConditionsByTrail(mTrailId, 0, 5);

				if (conditions.size() > 0) {
					mLastCondition = conditions.get(0);
				}

				for (Condition condition : conditions) {
					condition.upsert(resolver);
				}

			} catch (CredentialsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SmartTrailException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			if (mLastCondition != null) {
				setTrailStatus(mLastCondition.mStatus);
			}
		}

		@Override
		protected void onCancelled() {

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

		mMap.setMyLocationEnabled(false);
		SmartTrailApplication app = (SmartTrailApplication) getActivity()
				.getApplication();
		LatLng latLng = app.getLastLng();
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 13.0f);
		mMap.animateCamera(update);
		mMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng point) {
				startMap();

			}
		});
		mMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				startMap();
				return true;
			}
		});
		UiSettings uiSettings = mMap.getUiSettings();
		uiSettings.setZoomControlsEnabled(false);
		uiSettings.setAllGesturesEnabled(false);

	}

	public void startMap() {
		Fragment frag = new TrailMapFragment();

		Bundle args = new Bundle();
		args.putString(TrailMapFragment.EXTRA_TRAIL_ID, mTrailId);
		// args.putParcelable(AddConditionFragment.EXTRA_TRAILS_URI,
		// AreasSchema.buildTrailsUri(mAreaId));
		frag.setArguments(args);

		MainActivity activity = (MainActivity) getActivity();
		activity.switchSubContent(frag, "AddConditionFragment");
	}

	/**
	 * Update trails from the database and redraw.
	 * 
	 */
	private class RefreshTrailsTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected Boolean doInBackground(Void... params) {

			mMapData.readTrailData(getActivity(), mAreaId);
			return true;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			draw_trails();
			mMapData.setTrailWidth(mTrailId, 13.0f);
		}

		@Override
		protected void onCancelled() {
		}
	}

	private void addTrailsToMap() {

		for (MapTrail trail : mMapData.mTrails) {
			trail.mMapPolyline = mMap.addPolyline(trail.mMapLine);

			for (MarkerOptions mo : trail.mTrailheadMarkers) {
				Marker marker = mMap.addMarker(mo);
				// mMarkerMap.put(marker.getId(), new TrailMarker(
				// trail.mTrail.mId));
			}

			// difficulty marker
			if (trail.mDifficultyMarker != null) {
				Marker marker = mMap.addMarker(trail.mDifficultyMarker);
				// mMarkerMap.put(marker.getId(), new TrailMarker(
				// trail.mTrail.mId));
			}
		}

	}

	// private void animateToAreaCenter(float zoom) {
	// LatLng center = mMapData.mAreas.get(0).mCenter;
	// mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, zoom));
	// }

	// private void animateToAreaCenter() {
	// float zoom = 12.0f;
	// // if (!mFirstZoom) {
	// // zoom = mMap.getCameraPosition().zoom;
	// // mFirstZoom = false;
	// // }
	// animateToAreaCenter(zoom);
	// }

	private void draw_trails() {
		// animateToAreaCenter(12.0f);
		clearMap();
		addTrailsToMap();

	}

	private void clearMap() {
		mMap.clear();
		// mMarkerMap.clear();
	}
}
