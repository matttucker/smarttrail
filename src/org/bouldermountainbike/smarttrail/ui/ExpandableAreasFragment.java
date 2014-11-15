package org.bouldermountainbike.smarttrail.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.bouldermountainbike.smarttrail.R;
import org.bouldermountainbike.smarttrail.app.Constants;
import org.bouldermountainbike.smarttrail.app.Preferences;
import org.bouldermountainbike.smarttrail.app.SmartTrailApplication;
import org.bouldermountainbike.smarttrail.error.CredentialsException;
import org.bouldermountainbike.smarttrail.error.SmartTrailException;
import org.bouldermountainbike.smarttrail.model.Condition;
import org.bouldermountainbike.smarttrail.model.Trail;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.AreasColumns;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.AreasSchema;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.RegionsSchema;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.TrailsColumns;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.TrailsSchema;
import org.bouldermountainbike.smarttrail.util.TimeUtil;
import org.json.JSONException;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

/**
 * Demonstrates expandable lists backed by Cursors
 */
public class ExpandableAreasFragment extends Fragment implements
		OnChildClickListener, OnGroupExpandListener {

	private static final String[] AREAS_PROJECTION = new String[] {
			AreasSchema._ID, AreasSchema.AREA_ID, AreasSchema.NAME };
	private static final int GROUP_ID_COLUMN_INDEX = 1;

	private static final int TOKEN_GROUP = 0;
	private static final int TOKEN_CHILD = 1;

	private RefreshTrailConditionsTask mRefreshTrailConditionsTask;

	private static ExpandableAreasFragment mInstance;
	private String mAreaId;

	private Menu mOptionsMenu;

	private QueryHandler mQueryHandler;
	private CursorTreeAdapter mAdapter;
	private SmartTrailApplication mApp;
	private static Preferences mPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set up our adapter
		mAdapter = new MyExpandableListAdapter(getActivity(),
				android.R.layout.simple_expandable_list_item_1,
				R.layout.list_item_trail2,
				new String[] { AreasSchema.NAME }, // Name for group layouts
				new int[] { android.R.id.text1 },
				new String[] { TrailsSchema.NAME }, // Trail name
				new int[] { android.R.id.text1 });

		mQueryHandler = new QueryHandler(getActivity(), mAdapter);

		mApp = (SmartTrailApplication) getActivity().getApplication();
		Uri uri = RegionsSchema.buildAreasUri("1");
		mInstance = this;
		// Query for areas
		mQueryHandler.startQuery(TOKEN_GROUP, null, uri, AREAS_PROJECTION,
				null, null, AreasColumns.NAME + " COLLATE LOCALIZED ASC");
		setHasOptionsMenu(true);

		mPreferences = Preferences.getInstance(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ExpandableListView listView = (ExpandableListView) inflater.inflate(
				R.layout.list_areas, null);
		listView.setAdapter(mAdapter);
		listView.setOnGroupExpandListener(this);
		listView.setOnChildClickListener(this);
		getActivity().getActionBar().setTitle(R.string.title_trail_areas); 
		getActivity().getActionBar().setNavigationMode(
				ActionBar.NAVIGATION_MODE_STANDARD);


		return listView;
	}

	@Override
	public void onResume() {
		super.onResume();
		boolean refresh=((ResultHandler) getActivity()).getAndClearRefresh();
		if (refresh) {
			mQueryHandler.mRefreshing = false;
			mAdapter.notifyDataSetChanged();
			
		}
	}
	
	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

		mOptionsMenu = menu;
		inflater.inflate(R.menu.map_menu, menu);
		inflater.inflate(R.menu.refresh_menu_items, menu);

	}

	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case android.R.id.home:
			return super.onOptionsItemSelected(item);

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
			if (mAreaId != null) {
				Fragment frag = new AddConditionFragment();

				Bundle args = new Bundle();
				args.putString(AddConditionFragment.EXTRA_TRAILS_ID, "");
				args.putParcelable(AddConditionFragment.EXTRA_TRAILS_URI,
						AreasSchema.buildTrailsUri(mAreaId));
				frag.setArguments(args);

				MainActivity activity = (MainActivity) getActivity();
				activity.switchSubContent(frag, "AddConditionFragment");
			}

		} else {
			Fragment frag = new SigninFragment();

			Bundle args = new Bundle();
			frag.setArguments(args);

			MainActivity activity = (MainActivity) getActivity();
			activity.switchSubContent(frag, "SigninFragment");

		}
	}

	void startRefreshTrailConditions(ArrayList<String> ids) {
		if (mRefreshTrailConditionsTask != null) {
			mRefreshTrailConditionsTask.cancel(true);
		}
		mRefreshTrailConditionsTask = new RefreshTrailConditionsTask();
		mRefreshTrailConditionsTask.execute(ids);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {

		Cursor cursor = (Cursor) parent.getExpandableListAdapter().getChild(
				groupPosition, childPosition);

		final String trailId = cursor.getString(cursor
				.getColumnIndex(TrailsColumns.TRAIL_ID));

		Fragment fragment = new TrailDetailFragment();
		Bundle args = new Bundle();
		args.putString(TrailDetailFragment.EXTRA_TRAIL_ID, trailId);
		fragment.setArguments(args);

		MainActivity activity = (MainActivity) getActivity();
		activity.switchSubContent(fragment, "trailDetailFragment");
		return false;
	}

	@Override
	public void onGroupExpand(int groupPosition) {

		mPreferences.saveAreaPosition(groupPosition);
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

	public interface TrailsQuery {
		int TOKEN = 0x1;

		String[] PROJECTION = { TrailsSchema._ID, TrailsSchema.TRAIL_ID,
				TrailsSchema.NAME, TrailsColumns.AREA_ID,
				TrailsColumns.CONDITION, TrailsColumns.STATUS_UPDATED_AT };

		int _ID = 0;
		int TRAIL_ID = 1;
		int NAME = 2;
		int AREA_ID = 3;
		int STATUS = 4;
		int UPDATED_AT = 5;
	}

	/**
	 * Update trails from the database and redraw.
	 * 
	 */
	private class RefreshTrailConditionsTask extends
			AsyncTask<ArrayList<String>, Void, Boolean> {

		@Override
		protected void onPreExecute() {

			setLoading(true);
		}

		@Override
		protected Boolean doInBackground(ArrayList<String>... params) {

			refreshTrailConditions(params[0]);

			return true;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			mAdapter.notifyDataSetChanged();
			setLoading(false);
		}

		@Override
		protected void onCancelled() {
		}
	}

	public void refreshTrailConditions(ArrayList<String> trailIds) {
		SmartTrailApplication app = (SmartTrailApplication) getActivity()
				.getApplication();
		ContentResolver resolver = getActivity().getContentResolver();
		Trail trail = new Trail();
		for (String trailId : trailIds) {
			ArrayList<Condition> conditions = null;
			try {

				conditions = app.getApi().getConditionsByTrail(trailId, 0, 5);

				if (conditions.size() > 0) {
					Condition condition = conditions.get(0);
					trail.mId = trailId;
					trail.mStatus = condition.mStatus;
					trail.mStatusUpdatedAt = condition.mUpdatedAt;
					trail.updateStatus(resolver);
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

		}

	}

	private static final class QueryHandler extends AsyncQueryHandler {
		private CursorTreeAdapter mAdapter;
		private boolean mRefreshing = false;

		public QueryHandler(Context context, CursorTreeAdapter adapter) {
			super(context.getContentResolver());
			this.mAdapter = adapter;
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			switch (token) {
			case TOKEN_GROUP:
				mAdapter.setGroupCursor(cursor);
			
				break;

			case TOKEN_CHILD:
				int groupPosition = (Integer) cookie;

				if (!mRefreshing) {
					mRefreshing = true;
					// get trail ids;
					ArrayList<String> ids = new ArrayList<String>();
					try {
						while (cursor.moveToNext()) {
							ids.add(cursor.getString(1));
						}

					} finally {
						cursor.moveToFirst();
					}
					mInstance.startRefreshTrailConditions(ids);
				}
				mAdapter.setChildrenCursor(groupPosition, cursor);
				break;
			}
		}
	}

	public class MyExpandableListAdapter extends SimpleCursorTreeAdapter {

		// Note that the constructor does not take a Cursor. This is done to
		// avoid querying the database on the main thread.
		public MyExpandableListAdapter(Context context, int groupLayout,
				int childLayout, String[] groupFrom, int[] groupTo,
				String[] childrenFrom, int[] childrenTo) {

			super(context, null, groupLayout, groupFrom, groupTo, childLayout,
					childrenFrom, childrenTo);

		}

		@Override
		public void onGroupExpanded(int groupPosition) {
			mQueryHandler.mRefreshing = false;
		}

		@Override
		protected Cursor getChildrenCursor(Cursor groupCursor) {
			// Given the group, we return a cursor for all the children within
			// that group

			// Return a cursor that points to this areas trails
			// Uri.Builder builder = .CONTENT_URI.buildUpon();
			mAreaId = groupCursor.getString(GROUP_ID_COLUMN_INDEX);
			Uri uri = AreasSchema.buildTrailsUri(mAreaId);

			mQueryHandler.startQuery(TrailsQuery.TOKEN,
					groupCursor.getPosition(), uri, TrailsQuery.PROJECTION,
					null, null, null);

			return null;
		}

		@Override
		protected void bindChildView(View view, Context context, Cursor cursor,
				boolean isLastChild) {
			super.bindChildView(view, context, cursor, isLastChild);

			TextView time = (TextView) view.findViewById(R.id.updated_at);
			long updatedAt = cursor.getLong(TrailsQuery.UPDATED_AT);
			long now = new GregorianCalendar().getTimeInMillis();
			long delta = now - updatedAt;
			time.setText(TimeUtil.ago(updatedAt, delta));

			String status = cursor.getString(TrailsQuery.STATUS);
//			if (delta > 3*TimeUtil.DAY_MS) {
//				status = Condition.UNKNOWN;
//			}
			ImageView statusImage = (ImageView) view
					.findViewById(R.id.condition);
			statusImage.setImageDrawable(Condition.getStatusDrawable(context,
					status));
			final String trailId = cursor.getString(TrailsQuery.TRAIL_ID);
			final String areaId = cursor.getString(TrailsQuery.AREA_ID);

			statusImage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					Fragment frag = new ConditionsFragment();

					Bundle args = new Bundle();
					args.putString(TrailDescriptionFragment.EXTRA_TRAIL_ID,
							trailId);
					args.putString(Constants.EXTRA_AREA_ID, areaId);

					frag.setArguments(args);

					MainActivity activity = (MainActivity) getActivity();
					activity.switchSubContent(frag, "ConditionsFragment");
				}
			});
		}

		

	}

}