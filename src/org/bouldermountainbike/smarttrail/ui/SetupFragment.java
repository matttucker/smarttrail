/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package org.bouldermountainbike.smarttrail.ui;

import java.io.IOException;

import org.bouldermountainbike.smarttrail.R;
import org.bouldermountainbike.smarttrail.app.Preferences;
import org.bouldermountainbike.smarttrail.app.SmartTrailApplication;
import org.bouldermountainbike.smarttrail.io.AreasHandler;
import org.bouldermountainbike.smarttrail.io.LocalExecutor;
import org.bouldermountainbike.smarttrail.io.TrailsHandler;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.AreasSchema;
import org.bouldermountainbike.smarttrail.util.AppLog;
import org.json.JSONException;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SetupFragment extends Fragment {
	@SuppressWarnings("unused")
	private static final String CLASSTAG = SetupFragment.class.getSimpleName();

	public static final int MAPS_VERSION = 3;

	private ProgressBar mProgressBar;

	private AsyncTask<Void, Integer, Boolean> mSetupTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Retain this fragment across configuration changes.
	    setRetainInstance(true);
	    
	    

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		getActivity().getActionBar().setNavigationMode(
				ActionBar.NAVIGATION_MODE_STANDARD);
		getActivity().getActionBar().setTitle(R.string.app_name);

		View view = inflater.inflate(R.layout.fragment_setup, container, false);

		SmartTrailApplication app = (SmartTrailApplication) getActivity()
				.getApplication();

		TextView setupDescriptionTextView = ((TextView) view
				.findViewById(R.id.setupDescription));
		setupDescriptionTextView.setTypeface(app.mTf);

		mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		
		if (mSetupTask == null) {
			mSetupTask =new SetupTask();
			mSetupTask.execute();
		}
		return view;
	}

	/**
	 * 
	 * @author Matt Tucker (matt@geozen.com)
	 * 
	 */
	private class SetupTask extends AsyncTask<Void, Integer, Boolean> {

		private final String CLASSTAG = "SetupTask";

		@Override
		protected void onPreExecute() {
			mProgressBar.setProgress(0);
			mProgressBar.setMax(107);
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			final ContentResolver resolver = getActivity().getContentResolver();
			Context context = getActivity();
			SmartTrailApplication app = (SmartTrailApplication) getActivity()
					.getApplication();

			LocalExecutor localExecutor = new LocalExecutor(resolver);
			AreasHandler areasHandler = new AreasHandler();
			TrailsHandler trailsHandler = new TrailsHandler();

			int progress = 0;

			if (!AreasSchema.exists(resolver, "1249", MAPS_VERSION)) {
				try {
					localExecutor.execute(context,
							"trails/trail_caribou_ranch_link.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_kinnickinnick_loop.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_tungsten_loop.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context, "trails/area_mud_lake.json",
							areasHandler);
					publishProgress(++progress);
				} catch (JSONException e) {

					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				progress += 4;
				publishProgress(progress);
			}

			if (!AreasSchema.exists(resolver, "1472",MAPS_VERSION)) {
				try {
					localExecutor.execute(context,
							"trails/trail_canyon_loop.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_benjamin_loop.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_betasso_link.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_fourmile_link.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/area_betasso_preserve.json", areasHandler);
					publishProgress(++progress);
				} catch (JSONException e) {

					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				progress += 5;
				publishProgress(progress);
			}

			if (!AreasSchema.exists(resolver, "1474", MAPS_VERSION)) {
				try {
					localExecutor.execute(context,
							"trails/trail_bear_down___star_wars.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_blue_dot.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context, "trails/trail_boot.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context, "trails/trail_reboot.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_red_dot_yellow_dot_loop.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_doe___dog.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_powerline.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_lollypop_loop.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context, "trails/trail_carwash.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor
							.execute(context,
									"trails/trail_front_range_road.json",
									trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_big_springs.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_wildwood.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/area_dot_trails.json", areasHandler);
					publishProgress(++progress);
				} catch (JSONException e) {

					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				progress += 13;
				publishProgress(progress);
			}

			if (!AreasSchema.exists(resolver, "1475", MAPS_VERSION)) {
				try {
					localExecutor.execute(context,
							"trails/trail_foothills.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context, "trails/trail_eagle.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_north_rim.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_left_hand.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context, "trails/trail_sage.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_boulder_reservoir.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor
							.execute(
									context,
									"trails/area_boulder_valley_ranch___boulder_reservoir.json",
									areasHandler);
					publishProgress(++progress);
				} catch (JSONException e) {

					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				progress += 7;
				publishProgress(progress);
			}

			if (!AreasSchema.exists(resolver, "1476",MAPS_VERSION)) {
				try {
					localExecutor.execute(context,
							"trails/trail_community_ditch__west_.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_doudy_draw.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_flatirons_vista_south.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_prairie_vista.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_spring_brook_north.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_flatirons_vista_north.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_spring_brook_south.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/area_doudy_draw.json", areasHandler);
					publishProgress(++progress);
				} catch (JSONException e) {

					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				progress += 8;
				publishProgress(progress);
			}

			if (!AreasSchema.exists(resolver, "1477", MAPS_VERSION)) {
				try {
					localExecutor.execute(context,
							"trails/trail_cottonwood.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_east_boulder___teller_farm_.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_south_boulder_creek.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_east_boulder___white_rocks_.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_coal_creek.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context, "trails/trail_niwot_s.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_pella_crossing.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_rock_creek.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_east_boulder___gunbarrel_.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_cherryvale.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/area_east_boulder_county.json",
							areasHandler);
					publishProgress(++progress);
				} catch (JSONException e) {

					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				progress += 11;
				publishProgress(progress);
			}

			if (!AreasSchema.exists(resolver, "1478", MAPS_VERSION)) {
				try {
					localExecutor.execute(context,
							"trails/trail_antelope.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_lower_bitterbrush.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_upper_bitterbrush.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_nelson_loop.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/area_hall_ranch.json", areasHandler);
					publishProgress(++progress);
				} catch (JSONException e) {

					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				progress += 5;
				publishProgress(progress);
			}

			if (!AreasSchema.exists(resolver, "1479", MAPS_VERSION)) {
				try {
					localExecutor.execute(context,
							"trails/trail_picture_rock.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_ponderosa_loop.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context, "trails/trail_wapiti.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_wild_turkey.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/area_heil_valley_ranch.json", areasHandler);
					publishProgress(++progress);
				} catch (JSONException e) {

					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				progress += 5;
				publishProgress(progress);
			}

			if (!AreasSchema.exists(resolver, "1480", MAPS_VERSION)) {
				try {
					localExecutor.execute(context,
							"trails/trail_buchanan_pass.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_beaver_reservoir_cutoff.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor
							.execute(context,
									"trails/trail_coney_flats_road.json",
									trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_505_road.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/area_high_country.json", areasHandler);
					publishProgress(++progress);
				} catch (JSONException e) {

					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				progress += 5;
				publishProgress(progress);
			}

			if (!AreasSchema.exists(resolver, "1481", MAPS_VERSION)) {
				try {
					localExecutor.execute(context, "trails/trail_coalton.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_coal_seam.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_cowdrey_draw.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_greenbelt_plateau.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_high_plains.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_marshall_valley.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_mayhoffer-singletree.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_community_ditch__east_.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_meadowlark.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/area_marshall_mesa.json", areasHandler);
					publishProgress(++progress);
				} catch (JSONException e) {

					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				progress += 10;
				publishProgress(progress);
			}

			if (!AreasSchema.exists(resolver, "1482", MAPS_VERSION)) {
				try {
					localExecutor.execute(context,
							"trails/trail_ceran_st_vrain____miller_rock.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_switzerland___north_.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_pennsylvania_gulch.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_switzerland___south_.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_chapman_drive.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/area_middle_country.json", areasHandler);
					publishProgress(++progress);
				} catch (JSONException e) {

					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				progress += 6;
				publishProgress(progress);
			}

			if (!AreasSchema.exists(resolver, "1483", MAPS_VERSION)) {
				try {
					localExecutor.execute(context,
							"trails/trail_eagle_wind.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_indian_mesa.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_little_thompson_overlook.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/area_rabbit_mountain.json", areasHandler);
					publishProgress(++progress);
				} catch (JSONException e) {

					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				progress += 4;
				publishProgress(progress);
			}

			if (!AreasSchema.exists(resolver, "1484", MAPS_VERSION)) {
				try {
					localExecutor
							.execute(context,
									"trails/trail_meyers_homestead.json",
									trailsHandler);
					publishProgress(++progress);
					localExecutor
							.execute(context,
									"trails/trail_walker_connector.json",
									trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_walker_ranch_loop.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/area_walker_ranch.json", areasHandler);
					publishProgress(++progress);
				} catch (JSONException e) {

					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				progress += 4;
				publishProgress(progress);
			}

			if (!AreasSchema.exists(resolver, "1485",MAPS_VERSION)) {
				try {
					localExecutor.execute(context,
							"trails/trail_little_raven.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_south_st_vrain___east_.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_sourdough___north_.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_sourdough___south_.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_south_st_vrain___west_.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_wapiti___usfs_.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context, "trails/trail_waldrop.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_left_hand_res_road.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_little_raven__east_.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/area_brainard_lake.json", areasHandler);
					publishProgress(++progress);
				} catch (JSONException e) {

					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				progress += 10;
				publishProgress(progress);
			}

			if (!AreasSchema.exists(resolver, "1757", MAPS_VERSION)) {
				try {
					localExecutor.execute(context,
							"trails/trail_aspen_alley.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context, "trails/trail_hobbit.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context, "trails/trail_lookout.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_observatory.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_pungy_stick.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context, "trails/trail_re-root.json",
							trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_school_bus.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/trail_sugar_mag.json", trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context,
							"trails/area_west_magnolia.json", areasHandler);
					publishProgress(++progress);
				} catch (JSONException e) {

					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				progress += 9;
				publishProgress(progress);
			}

			if (!AreasSchema.exists(resolver, "2666", MAPS_VERSION)) {
				try {
					localExecutor
							.execute(context,
									"trails/trail_sunset_park_west.json",
									trailsHandler);
					publishProgress(++progress);
					localExecutor.execute(context, "trails/area_erie.json",
							areasHandler);
					publishProgress(++progress);
				} catch (JSONException e) {

					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				progress += 2;
				publishProgress(progress);
			}

			app.mPrefs.edit()
					.putInt(Preferences.PREF_MAPS_VERSION, MAPS_VERSION)
					.commit();
			return true;
		}

		@Override
		protected void onProgressUpdate(final Integer... values) {
			mProgressBar.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(Boolean done) {
			AppLog.d(CLASSTAG, "onPostExecute(): " + done);
 			((MainActivity) getActivity()).selectItem(MainActivity.DRAWER_MAP_POSITION);

		}

		@Override
		protected void onCancelled() {
		}
	}

}