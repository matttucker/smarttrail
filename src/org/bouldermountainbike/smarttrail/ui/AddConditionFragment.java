/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package org.bouldermountainbike.smarttrail.ui;

import org.bouldermountainbike.smarttrail.R;
import org.bouldermountainbike.smarttrail.api.SmartTrailApi;
import org.bouldermountainbike.smarttrail.app.SmartTrailApplication;
import org.bouldermountainbike.smarttrail.model.Condition;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.TrailsColumns;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.TrailsSchema;
import org.bouldermountainbike.smarttrail.util.AppLog;
import org.bouldermountainbike.smarttrail.util.NotificationsUtil;
import org.bouldermountainbike.smarttrail.util.NotifyingAsyncQueryHandler;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
//import android.app.Dialog;
//import android.app.DialogFragment;

public class AddConditionFragment extends Fragment implements
		NotifyingAsyncQueryHandler.AsyncQueryListener {

	public static final String EXTRA_TRAILS_URI = "org.bouldermountainbike.smarttrail.extra.TRAILS_URI";

	public static final String EXTRA_TRAILS_ID = "org.bouldermountainbike.smarttrail.extra.TRAILS_ID";

	private PushConditionTask mPushConditionTask;

	private ArrayAdapter<CharSequence> mStatusAdapter;

	private EditText mComment;
	private NotifyingAsyncQueryHandler mHandler;

	private String[] mTrailNames;
	private String[] mTrailIds;
	private boolean[] mTrailSelections;

	private AlertDialog mTrailSelectDialog;

	private TextView mTrailsTextView;

	private String mInitialTrailsId;


	private ConditionView mConditionView;


	// Container Activity must implement this interface
	public interface OnAddConditionListener {
		public void onConditionSubmitted();

		public void onConditionCancelled();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new NotifyingAsyncQueryHandler(getActivity()
				.getContentResolver(), this);

		Bundle arguments = getArguments();

		mHandler.cancelOperation(TrailsQuery._TOKEN);

		mInitialTrailsId = arguments.getString(EXTRA_TRAILS_ID);
		final Uri trailsUri = (Uri) arguments.getParcelable(EXTRA_TRAILS_URI);

		if (trailsUri == null) {
			return;
		}

		// Start background query to load trails
		mHandler.startQuery(TrailsQuery._TOKEN, null, trailsUri,
				TrailsQuery.PROJECTION, null, null, TrailsSchema.DEFAULT_SORT);

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		getActivity().getActionBar().setNavigationMode(
				ActionBar.NAVIGATION_MODE_STANDARD);
		getActivity().getActionBar().setTitle(R.string.title_add_condition);
		View v = inflater.inflate(R.layout.fragment_add_condition, container,
				false);

		mStatusAdapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.conditions, android.R.layout.simple_spinner_item);
		mStatusAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		mConditionView = (ConditionView) v.findViewById(R.id.conditionView);

		mComment = (EditText) v.findViewById(R.id.comment);
		mTrailsTextView = (TextView) v.findViewById(R.id.trails);
		//
		// Confirm
		//
		Button submitButton = (Button) v.findViewById(R.id.confirm);
		submitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// dismiss softkeyboard
				InputMethodManager imm = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mComment.getWindowToken(), 0);

				if (mConditionView.getStatus().equals(Condition.CLOSED)) {

					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivity());
					AlertDialog frag = builder
							.setMessage(R.string.confirm_closed)
							.setNegativeButton(android.R.string.no,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											mConditionView.setStatus(2);
										}
									})

							.setPositiveButton(android.R.string.ok,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											mPushConditionTask = new PushConditionTask();
											mPushConditionTask.execute();
										}
									}).create();

					frag.show();

				} else {

					mPushConditionTask = new PushConditionTask();
					mPushConditionTask.execute();
				}

			}

		});

		//
		// Cancel
		//
		Button cancel = (Button) v.findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// dismiss softkeyboard
				InputMethodManager imm = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mComment.getWindowToken(), 0);

				getActivity().onBackPressed();
			}
		});

		LinearLayout trailsContainer = (LinearLayout) v
				.findViewById(R.id.trailsContainer);

		trailsContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (mTrailSelectDialog != null)
					mTrailSelectDialog.show();
			}
		});

		return v;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			getActivity().onBackPressed();
			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	private class PushConditionTask extends AsyncTask<Void, Void, Boolean> {

		private final String CLASSTAG = "PushConditionTask";

		private final ProgressDialog progressDialog = new ProgressDialog(
				getActivity());

		private Exception mReason;

		@Override
		protected void onPreExecute() {
			this.progressDialog
					.setMessage(getText(R.string.submittingCondition));
			this.progressDialog.show();

		}

		@Override
		protected Boolean doInBackground(Void... params) {

			try {
				SmartTrailApplication app = (SmartTrailApplication) getActivity()
						.getApplication();
				SmartTrailApi api = app.getApi();
				Condition condition = new Condition();

				condition.mStatus = mConditionView.getStatus();

				condition.mComment = mComment.getText().toString();

				// push all the selected trails
				for (int i = 0; i < mTrailSelections.length; i++) {
					if (mTrailSelections[i]) {
						condition.mTrailId = mTrailIds[i];
						api.pushCondition(app.mUsername, app.mEmail, condition);
					}

				}

			} catch (Exception e) {

				AppLog.e(CLASSTAG, "Exception adding condition.", e);
				mReason = e;
				return false;
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			mPushConditionTask = null;
			this.progressDialog.cancel();
			
			if (success) {
				Activity activity = getActivity();
				((ResultHandler) activity).setRefresh(true);
				activity.onBackPressed();
				
			} else {
				NotificationsUtil.ToastReasonForFailure(getActivity(), mReason);

			}

			

			
		}

		@Override
		protected void onCancelled() {
			this.progressDialog.cancel();

		}
	}

	/**
	 * {@link org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.TrailsSchema}
	 * query parameters.
	 */
	private interface TrailsQuery {
		int _TOKEN = 0x1;

		String[] PROJECTION = { BaseColumns._ID, TrailsColumns.TRAIL_ID,
				TrailsColumns.NAME };

		@SuppressWarnings("unused")
		int _ID = 0;
		int TRAIL_ID = 1;
		int NAME = 2;

	}

	@Override
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		if (getActivity() == null) {
			return;
		}

		if (cursor != null) {
			if (token == TrailsQuery._TOKEN) {

				//
				// Construct arrays from the trails cursor for the trails
				// selection dialog.
				//
				mTrailNames = new String[cursor.getCount()];
				mTrailIds = new String[cursor.getCount()];
				mTrailSelections = new boolean[mTrailNames.length];

				try {
					int i = 0;
					while (cursor.moveToNext()) {
						mTrailIds[i] = cursor.getString(TrailsQuery.TRAIL_ID);
						mTrailNames[i] = cursor.getString(TrailsQuery.NAME);

						if (TextUtils.isEmpty(mInitialTrailsId)) {
							mTrailSelections[i] = true;
						} else if (mTrailIds[i].equals(mInitialTrailsId)) {
							mTrailSelections[i] = true;
						}
						i++;
					}

					final boolean[] tmpSelections = new boolean[mTrailSelections.length];
					System.arraycopy(mTrailSelections, 0, tmpSelections, 0,
							mTrailSelections.length);

					mTrailSelectDialog = new AlertDialog.Builder(getActivity())
							.setTitle(getText(R.string.selectTrailsTitle))
							.setMultiChoiceItems(
									mTrailNames,
									mTrailSelections,
									new DialogInterface.OnMultiChoiceClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which, boolean isChecked) {
											tmpSelections[which] = isChecked;

										}
									})
							.setPositiveButton(R.string.ok,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											mTrailSelections = tmpSelections;
											constructSelectedTrails();
										}

									}).create();

					constructSelectedTrails();
				} finally {
					cursor.close();
				}

			} else {

				cursor.close();
			}
		}

	}

	private void constructSelectedTrails() {
		boolean first = true;
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < mTrailSelections.length; i++)
			if (mTrailSelections[i]) {
				if (first) {
					first = false;
				} else {
					buf.append(",");
				}
				buf.append(mTrailNames[i]);
			}

		mTrailsTextView.setText(buf.toString());
	}

	public static class ConfirmedClosedDialogFragment extends DialogFragment {

		public static ConfirmedClosedDialogFragment newInstance(int title) {
			ConfirmedClosedDialogFragment frag = new ConfirmedClosedDialogFragment();
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			return builder
					.setMessage("Are you sure you want to reset the count?")
					.setNegativeButton(android.R.string.no,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {

								}
							})

					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {

								}
							}).create();
		}
	}
}
