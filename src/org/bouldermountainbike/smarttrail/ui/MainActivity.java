package org.bouldermountainbike.smarttrail.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.bouldermountainbike.smarttrail.R;
import org.bouldermountainbike.smarttrail.app.Preferences;
import org.bouldermountainbike.smarttrail.app.SmartTrailApplication;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * This example illustrates a common usage of the DrawerLayout widget in the
 * Android support library.
 * <p/>
 * <p>
 * When a navigation (left) drawer is present, the host activity should detect
 * presses of the action bar's Up affordance as a signal to open and close the
 * navigation drawer. The ActionBarDrawerToggle facilitates this behavior. Items
 * within the drawer should fall into one of two categories:
 * </p>
 * <p/>
 * <ul>
 * <li><strong>View switches</strong>. A view switch follows the same basic
 * policies as list or tab navigation in that a view switch does not create
 * navigation history. This pattern should only be used at the root activity of
 * a task, leaving some form of Up navigation active for activities further down
 * the navigation hierarchy.</li>
 * <li><strong>Selective Up</strong>. The drawer allows the user to choose an
 * alternate parent for Up navigation. This allows a user to jump across an
 * app's navigation hierarchy at will. The application should treat this as it
 * treats Up navigation from a different task, replacing the current task stack
 * using TaskStackBuilder or similar. This is the only form of navigation drawer
 * that should be used outside of the root activity of a task.</li>
 * </ul>
 * <p/>
 * <p>
 * Right side drawers should be used for actions, not navigation. This follows
 * the pattern established by the Action Bar that navigation should be to the
 * left and actions to the right. An action should be an operation performed on
 * the current contents of the window, for example enabling or disabling a data
 * overlay on top of the current content.
 * </p>
 */
public class MainActivity extends Activity implements ResultHandler {
	public static final int DRAWER_MAP_POSITION = 1;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;

	SyncStatusUpdaterFragment mSyncStatusUpdaterFragment;
	private Fragment mFragment;
	private boolean mIsDataLoaded;
	private Preferences mPreferences;
	private boolean mRefresh;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final SmartTrailApplication app = (SmartTrailApplication) getApplication();

		mPreferences = Preferences.getInstance(this);
		mTitle = mDrawerTitle = getTitle();
		// mPlanetTitles = getResources().getStringArray(R.array.planets_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer
		// opens
		// mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
		// GravityCompat.START);
		// set up the drawer's list view with items and click listener
		// mDrawerList.setAdapter(new ArrayAdapter<String>(this,
		// R.layout.drawer_list_item, mPlanetTitles));
		// mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		SlidingMenuAdapter adapter = new SlidingMenuAdapter(this);

		adapter.add(new SlidingMenuItem("header",
				R.drawable.drawer_map, TrailMapFragment.class.getName()));
		
		adapter.add(new SlidingMenuItem(getString(R.string.title_map),
				R.drawable.drawer_map, TrailMapFragment.class.getName()));

		adapter.add(new SlidingMenuItem(getString(R.string.title_trails),
				R.drawable.drawer_trails, ExpandableAreasFragment.class
						.getName()));

		adapter.add(new SlidingMenuItem(getString(R.string.title_preferences),
				R.drawable.drawer_settings, MyPreferenceFragment.class
						.getName()));
		mDrawerList.setAdapter(adapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_navigation_drawer, /*
										 * nav drawer image to replace 'Up'
										 * caret
										 */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				// getActionBar().setTitle(mTitle);
				if (mFragment != null) {
					mFragment.setHasOptionsMenu(true);
				}
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setNavigationMode(
						ActionBar.NAVIGATION_MODE_STANDARD);
				getActionBar().setTitle(mDrawerTitle);

				final int titleId = Resources.getSystem().getIdentifier(
						"action_bar_title", "id", "android");
				TextView title = (TextView) getWindow().findViewById(titleId);

				title.setTypeface(app.mTf);

				if (mFragment != null) {
					mFragment.setHasOptionsMenu(false);
				}

				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		// turn on the Navigation Drawer image depending on where we are in the
		// stack
		mDrawerToggle.setDrawerIndicatorEnabled(getFragmentManager()
				.getBackStackEntryCount() == 0);

		int mapVersion = app.mPrefs.getInt(Preferences.PREF_MAPS_VERSION, 0);
		mIsDataLoaded = (mapVersion >= SetupFragment.MAPS_VERSION);

		if (!mIsDataLoaded) {

			FragmentManager fragmentManager = getFragmentManager();
			Fragment frag = (Fragment) fragmentManager
					.findFragmentByTag("setup");

			if (frag == null) {
				frag = new SetupFragment();
			}

			fragmentManager.beginTransaction()
					.replace(R.id.content_frame, frag, "setup").commit();

		} else if (savedInstanceState == null) {

			selectItem(mPreferences.getDrawerPosition());
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * On Android 3.0 and higher, the options menu is considered to always be
	 * open when menu items are presented in the action bar. When an event
	 * occurs and you want to perform a menu update, you must call
	 * invalidateOptionsMenu() to request that the system call
	 * onPrepareOptionsMenu().
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final SmartTrailApplication app = (SmartTrailApplication) getApplication();
		int mapVersion = app.mPrefs.getInt(Preferences.PREF_MAPS_VERSION, 0);
		mIsDataLoaded = (mapVersion >= SetupFragment.MAPS_VERSION);
		if (!mIsDataLoaded) {
			return false;
		}
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action buttons
		switch (item.getItemId()) {
		// case R.id.action_websearch:
		// // create intent to perform web search for this planet
		// Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
		// intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
		// // catch event that there's no activity to handle intent
		// if (intent.resolveActivity(getPackageManager()) != null) {
		// startActivity(intent);
		// } else {
		// Toast.makeText(this, R.string.app_not_available,
		// Toast.LENGTH_LONG).show();
		// }
		// return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* The click listner for ListView in the navigation drawer */
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	void selectItem(int position) {
		
		//Skip the header
		if (position == 0){
			return;
		}
		mPreferences.saveDrawerPosition(position);

		final SlidingMenuItem smi = (SlidingMenuItem) mDrawerList.getAdapter()
				.getItem(position);

		try {
			final Class<?> c = Class.forName(smi.fragmentClassName);
			final Constructor<?> constructor = c.getConstructor();

			mFragment = (Fragment) constructor.newInstance();

			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.content_frame, mFragment).commit();

			mDrawerLayout.closeDrawer(mDrawerList);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (java.lang.InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

	}

	public void switchSubContent(Fragment fragment, String name) {
		mDrawerToggle.setDrawerIndicatorEnabled(false);
		FragmentManager fm = getFragmentManager();

		fm.beginTransaction().replace(R.id.content_frame, fragment, name)
				.addToBackStack(name).commit();
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
		final int titleId = Resources.getSystem().getIdentifier(
				"action_bar_title", "id", "android");
		TextView titleView = (TextView) getWindow().findViewById(titleId);
		SmartTrailApplication app = (SmartTrailApplication) getApplication();
		titleView.setTypeface(app.mTf);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		if (getFragmentManager().getBackStackEntryCount() == 0) {
			// turn on the Navigation Drawer image
			mDrawerToggle.setDrawerIndicatorEnabled(true);
		}

	}

	private static class SlidingMenuItem {
		public String tag;
		public int iconRes;
		public String fragmentClassName;

		public SlidingMenuItem(String tag, int iconRes, String fragmentClassName) {
			this.tag = tag;
			this.iconRes = iconRes;
			this.fragmentClassName = fragmentClassName;
		}
	}

	public class SlidingMenuAdapter extends ArrayAdapter<SlidingMenuItem> {

		private static final int VIEW_TYPE_HEADER = 0;
		private static final int VIEW_TYPE_ROW = 1;

		public SlidingMenuAdapter(Context context) {
			super(context, 0);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			int viewType = getItemViewType(position);
			ViewHolder holder = null;
			if (convertView == null) {
				switch (viewType) {
				case VIEW_TYPE_HEADER:
					convertView = LayoutInflater.from(getContext()).inflate(
							R.layout.drawer_header, null);
					break;
				default:
				case VIEW_TYPE_ROW:
					convertView = LayoutInflater.from(getContext()).inflate(
							R.layout.row, null);
					holder = new ViewHolder();
					holder.textView = (TextView) convertView
							.findViewById(R.id.row_title);
					holder.imageView = (ImageView) convertView
							.findViewById(R.id.row_icon);
					convertView.setTag(holder);
				}
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			switch (viewType) {
			case VIEW_TYPE_HEADER:
				break;
				
			default:
			case VIEW_TYPE_ROW:
				holder.imageView.setImageResource(getItem(position).iconRes);
				holder.textView.setText(getItem(position).tag);

				SmartTrailApplication app = (SmartTrailApplication) getApplication();
				holder.textView.setTypeface(app.mTf);

			}

			return convertView;
		}

		@Override
		public int getItemViewType(int position) {
			// Define a way to determine which layout to use, here it's just
			// evens and odds.
			if (position == 0) {
				return VIEW_TYPE_HEADER;
			} else {
				return VIEW_TYPE_ROW;
			}
		}

		@Override
		public int getViewTypeCount() {
			return 2; // Count of different layouts
		}

	}

	public static class ViewHolder {
		public TextView textView;
		public ImageView imageView;
	}

	@Override
	public boolean getAndClearRefresh() {
		boolean refresh = mRefresh;
		mRefresh = false;
		return refresh;
	}

	@Override
	public void setRefresh(boolean refresh) {
		mRefresh = refresh;
		
	}

}