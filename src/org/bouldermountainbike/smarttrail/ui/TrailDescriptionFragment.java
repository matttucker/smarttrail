/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package org.bouldermountainbike.smarttrail.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.bouldermountainbike.smarttrail.R;
//import android.support.v4.app.Fragment;

public class TrailDescriptionFragment extends Fragment  {


	public static final String EXTRA_TRAIL_ID = "org.bouldermountainbike.smarttrail.extra.TRAIL_ID";
	public static final String EXTRA_TRAIL_NAME = "org.bouldermountainbike.smarttrail.extra.TRAIL_NAME";
	public static final String EXTRA_TRAIL_OVERVIEW = "org.bouldermountainbike.smarttrail.extra.TRAIL_OVERVIEW";



	//private String mTrailId;
	private String mTrailName;
	private String mOverview;



	private TextView mDescriptionTextView;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		reloadFromArguments(getArguments());
		setHasOptionsMenu(true);
	}

	public void reloadFromArguments(Bundle arguments) {

		// Load new arguments
		//mTrailId = arguments.getString(EXTRA_TRAIL_ID);
		mTrailName = arguments.getString(EXTRA_TRAIL_NAME);
		mOverview = arguments.getString(EXTRA_TRAIL_OVERVIEW);

	}
	public boolean onOptionsItemSelected(MenuItem item) {

		// Intent intent;
		switch (item.getItemId()) {
		case android.R.id.home:
			getActivity().getFragmentManager().popBackStackImmediate();
			return true;

		default:
			return super.onOptionsItemSelected(item);

		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_overview, container,
				false);


//		TextView title = (TextView) v.findViewById(R.id.title);
//		title.setText(mTrailName);
//		SmartTrailApplication app = (SmartTrailApplication) getActivity().getApplication();
//		title.setTypeface(app.mTf);

		mDescriptionTextView = (TextView) v.findViewById(R.id.overview);
		mDescriptionTextView.setText(Html.fromHtml(mOverview));

		return v;
	}

	

	
	
}
