package org.bouldermountainbike.smarttrail.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.bouldermountainbike.smarttrail.R;
import org.bouldermountainbike.smarttrail.app.SmartTrailApplication;
import org.bouldermountainbike.smarttrail.service.SyncService;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.ListFragment;

public class SlidingMenuFragment extends ListFragment implements
		SyncStatusListener {

	private SlidingMenuAdapter mAdapter;
	private ProgressBar mSyncProgress;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.slidingmenu, null);

		mSyncProgress = (ProgressBar) view.findViewById(R.id.syncProgressBar);

		RelativeLayout mSyncView = (RelativeLayout) view
				.findViewById(R.id.sync);
		mSyncView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
//				((FragmentChangeActivity) getActivity()).triggerRefresh();

			}
		});

		return view;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new SlidingMenuAdapter(getActivity());

		mAdapter.add(new SlidingMenuItem(getString(R.string.title_map),
				R.drawable.map_icon_normal, TrailMapFragment.class.getName()));

		mAdapter.add(new SlidingMenuItem(getString(R.string.title_trails),
				R.drawable.trail_icon_normal, ExpandableAreasFragment.class
						.getName()));

		mAdapter.add(new SlidingMenuItem(getString(R.string.title_preferences),
				android.R.drawable.ic_menu_preferences,
				MyPreferenceFragment.class.getName()));
		setListAdapter(mAdapter);

	}

	public void onResume() {
		super.onResume();
//		((FragmentChangeActivity) getActivity()).mSyncStatusUpdaterFragment
//				.registerListener(this);
	}

	public void onPause() {
		super.onPause();
//		((FragmentChangeActivity) getActivity()).mSyncStatusUpdaterFragment
//				.unregisterListener(this);
	}

	/** {@inheritDoc} */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

	    final SlidingMenuItem smi = (SlidingMenuItem) getListAdapter().getItem(position);

        try
        {
            final Class<?> c = Class.forName(smi.fragmentClassName);
            final Constructor<?> constructor = c.getConstructor();

            final Fragment newContent = (Fragment) constructor.newInstance();

            ActionBar actionBar = getActivity().getActionBar();
            actionBar.setTitle(smi.tag);
            switchFragment(newContent, smi.fragmentClassName);

        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        } catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        } catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        } catch (java.lang.InstantiationException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        } catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }

	}

	// the meat of switching the above fragment
	private void switchFragment(Fragment fragment, String tag) {
		if (getActivity() == null)
			return;

//		if (getActivity() instanceof FragmentChangeActivity) {
//			FragmentChangeActivity fca = (FragmentChangeActivity) getActivity();
//			fca.switchMainContent(fragment, tag);
//		}
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

		private SmartTrailApplication mApp;

		public SlidingMenuAdapter(Context context) {
			super(context, 0);
			mApp = (SmartTrailApplication) getActivity().getApplication();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.row, null);
			}
			ImageView icon = (ImageView) convertView
					.findViewById(R.id.row_icon);
			icon.setImageResource(getItem(position).iconRes);
			TextView title = (TextView) convertView
					.findViewById(R.id.row_title);
			title.setText(getItem(position).tag);
			title.setTypeface(mApp.mTf);

			return convertView;
		}

	}

	@Override
	public void onStatusChanged(int status) {

		switch (status) {
		case SyncService.STATUS_RUNNING:
			mSyncProgress.setVisibility(View.VISIBLE);
			break;

		case SyncService.STATUS_FINISHED:
			mSyncProgress.setVisibility(View.INVISIBLE);
			break;
		}

	}
}
