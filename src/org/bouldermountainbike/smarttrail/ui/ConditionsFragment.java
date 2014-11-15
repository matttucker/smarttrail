/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package org.bouldermountainbike.smarttrail.ui;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import org.bouldermountainbike.smarttrail.R;
import org.bouldermountainbike.smarttrail.app.Constants;
import org.bouldermountainbike.smarttrail.app.SmartTrailApplication;
import org.bouldermountainbike.smarttrail.error.CredentialsException;
import org.bouldermountainbike.smarttrail.error.SmartTrailException;
import org.bouldermountainbike.smarttrail.model.Condition;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.AreasSchema;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.ConditionsColumns;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.TrailsSchema;
import org.bouldermountainbike.smarttrail.util.NotifyingAsyncQueryHandler;

/**
 * A simple {@link ListFragment} that renders a list of trail conditions using a
 * {@link ConditionsAdapter}.
 */
public class ConditionsFragment extends ListFragment implements
		NotifyingAsyncQueryHandler.AsyncQueryListener, OnRefreshListener {
	private ConditionsAdapter mAdapter;
	private AsyncQueryHandler mHandler;
	private String mTrailId;
	private PullToRefreshLayout mPullToRefreshLayout;
	private String mAreaId;
	private AsyncTask<Void, Void, Void> mRefreshTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			mTrailId = args.getString(TrailDescriptionFragment.EXTRA_TRAIL_ID);
			mAreaId = args.getString(Constants.EXTRA_AREA_ID);
		}

		mAdapter = new ConditionsAdapter(getActivity());
		setListAdapter(mAdapter);

		mHandler = new NotifyingAsyncQueryHandler(getActivity()
				.getContentResolver(), this);
		startConditionsQuery();

		setHasOptionsMenu(true);
	}

	void startConditionsQuery() {

		// String selection = ConditionsColumns.UPDATED_AT + " > "
		// + Long.toString(now.getTime() - 5 * TimeUtil.DAY_MS);
		// Start background queries to load trail/area/conditions details
		final Uri conditionsUri = TrailsSchema.buildConditionsDirUri(mTrailId);
		String selection = "";
		mHandler.startQuery(ConditionsQuery._TOKEN, null, conditionsUri,
				ConditionsQuery.PROJECTION, selection, null,
				ConditionsColumns.UPDATED_AT + " DESC");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ViewGroup root = (ViewGroup) inflater.inflate(
				R.layout.fragment_list_with_spinner, null);
		ActionBar actionBar = getActivity().getActionBar();
		getActivity().getActionBar().setTitle(R.string.conditions);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		return root;
	}

	public void onPause() {
		super.onPause();
		if (mRefreshTask != null) {
			mRefreshTask.cancel(true);
		}
	}
	public void onResume() {
		super.onResume();
		boolean refresh=((ResultHandler) getActivity()).getAndClearRefresh();
		if (refresh) {
			onRefreshStarted(null);
		}
	}
	@Override
	public void onViewCreated(View viewGroup, Bundle savedInstanceState) {
		super.onViewCreated(viewGroup, savedInstanceState);

		ViewGroup root = (ViewGroup) viewGroup;

		// View view = root.findViewById(R.id.ptr_layout);
		// As we're using a ListFragment we create a PullToRefreshLayout
		// manually
		mPullToRefreshLayout = new PullToRefreshLayout(root.getContext());

		// We can now setup the PullToRefreshLayout
		ActionBarPullToRefresh
				.from(getActivity())
				// We need to insert the PullToRefreshLayout into the Fragment's
				// ViewGroup
				.insertLayoutInto(root)
				// Here we mark just the ListView and it's Empty View as
				// pullable
				.theseChildrenArePullable(getListView(),
						getListView().getEmptyView()).listener(this)
				.setup(mPullToRefreshLayout);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.trail_detail_menu_items, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case android.R.id.home:
			getActivity().onBackPressed();
			return true;

		case R.id.menu_add_condition:

			startAddConditionFragment(R.id.content_frame);

			return true;
		default:
			return super.onOptionsItemSelected(item);

		}

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

	/** {@inheritDoc} */
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		if (getActivity() == null) {
			return;
		}
		if (cursor != null) {
			// getActivity().startManagingCursor(cursor);
			mAdapter.changeCursor(cursor);
			((BaseAdapter) getListAdapter()).notifyDataSetChanged();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		final Cursor cursor = (Cursor) mAdapter.getItem(position);

		if (cursor != null) {

			getListView().setItemChecked(position, true);

		}
	}

	private interface ConditionsQuery extends ConditionsAdapter.ConditionsQuery {
		int _TOKEN = 0x3;
	}

	@Override
	public void onRefreshStarted(View view) {

		//Cancel any previous refresh
		if (mRefreshTask != null) {
			mRefreshTask.cancel(true);
		}
		mRefreshTask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				SmartTrailApplication app = (SmartTrailApplication) getActivity()
						.getApplication();
				try {
					ArrayList<Condition> conditions = app.getApi()
							.getConditionsByTrail(mTrailId, 0, 5);
					ContentResolver resolver = getActivity()
							.getContentResolver();
					for (Condition condition : conditions) {
						condition.upsert(resolver);
					}

				} catch (CredentialsException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SmartTrailException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				startConditionsQuery();
				((BaseAdapter) getListAdapter()).notifyDataSetChanged();
				// Notify PullToRefreshLayout that the refresh has finished
				mPullToRefreshLayout.setRefreshComplete();

			}
		}.execute();

	}

}
