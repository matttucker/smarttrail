package org.bouldermountainbike.smarttrail.ui;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.bouldermountainbike.smarttrail.R;
import org.bouldermountainbike.smarttrail.app.SmartTrailApplication;

public class MyPreferenceFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {
	public static final String TAG = "MyPreferenceFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		setHasOptionsMenu(true);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ActionBar actionBar = getActivity().getActionBar();
		getActivity().getActionBar().setTitle(R.string.title_preferences);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

		return super.onCreateView(inflater, container, savedInstanceState);

	}

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences preferences = getPreferenceScreen()
				.getSharedPreferences();

		preferences.registerOnSharedPreferenceChangeListener(this);
		initSummaries(preferences);
	}

	private void initSummaries(SharedPreferences preferences) {

		//
		// MAP
		//
		String key = getString(R.string.pref_key_region);

		
		key = getString(R.string.pref_key_map_view);
		String value = preferences.getString(key,
				getString(R.string.pref_default_map_view));
		Preference connectionPref = findPreference(key);
		connectionPref.setSummary(value);
		
		//
		// ABOUT
		//
		key = getString(R.string.prefkey_version);
		String versionName;
		try {
			versionName = getActivity().getPackageManager()
				    .getPackageInfo(getActivity().getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			versionName="unknown";
		}
		connectionPref = findPreference(key);
		connectionPref.setSummary(versionName);
		
		final Preference eula = (Preference) findPreference(getString(R.string.prefkey_license_agreement));
		eula.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {

				Fragment frag = new WebFragment();

				Bundle args = new Bundle();
				String url = "file:///android_asset/eula.html";
				args.putCharSequence(WebFragment.EXTRA_URL, url);
				args.putCharSequence(WebFragment.EXTRA_TITLE, MyPreferenceFragment.this.getString(R.string.title_license_agreement));
				frag.setArguments(args);

				MainActivity activity = (MainActivity) getActivity();
				activity.switchSubContent(frag, "WebFragment");

				return true;
			}
		});
		//
		// OTHER
		//
		final Preference signinout = (Preference) findPreference(getString(R.string.prefkey_signout));
		final SmartTrailApplication app = (SmartTrailApplication) getActivity()
				.getApplication();
		if (app.isSignedin()) {
			signinout.setTitle(R.string.signout);
			signinout.setSummary("You are logged in as "+app.mUsername);
		} else {
			signinout.setTitle(R.string.signin);
		}

		signinout
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {
						if (app.isSignedin()) {
							app.signoutUser();
							signinout.setTitle(R.string.signin);
							signinout.setSummary("");
						} else {
							Fragment frag = new SigninFragment();

							Bundle args = new Bundle();
							frag.setArguments(args);

							MainActivity activity = (MainActivity) getActivity();
							activity.switchSubContent(frag, "SigninFragment");
							signinout.setTitle(R.string.signin);
						}
						return true;
					}
				});

	}

	@Override
	public void onPause() {
		super.onPause();

		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case android.R.id.home:
			// ((FragmentChangeActivity) getActivity()).toggleSlidingMenu();
			return true;

		default:
			return super.onOptionsItemSelected(item);

		}

	}

	public void onSharedPreferenceChanged(final SharedPreferences preferences,
			final String key) {
		initSummaries(preferences);

	}
}
