/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package org.bouldermountainbike.smarttrail.ui;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import org.bouldermountainbike.smarttrail.R;
import org.bouldermountainbike.smarttrail.app.SmartTrailApplication;
import org.bouldermountainbike.smarttrail.util.AppLog;
import org.bouldermountainbike.smarttrail.util.NotificationsUtil;

public class SigninFragment extends Fragment {
	@SuppressWarnings("unused")
	private static final String CLASSTAG = SigninFragment.class.getSimpleName();

	private ProgressDialog mProgressDialog;
	private TextView mUsernameEditText;
//	private TextView mPasswordEditText;
	private AsyncTask<Void, Void, Boolean> mSigninTask;
//	private Boolean mShow = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
		// Re-task if the request was cancelled.
//		mSigninTask = (SigninTask) getLastNonConfigurationInstance();
//		if (mSigninTask != null && mSigninTask.isCancelled()) {
//			AppLog.d(CLASSTAG, "LoginTask previously cancelled, trying again.");
//			mSigninTask = new SigninTask().execute();
//		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		getActivity().getActionBar().setNavigationMode(
				ActionBar.NAVIGATION_MODE_STANDARD);
		getActivity().getActionBar().setTitle(R.string.signIn);
		View view = inflater.inflate(R.layout.fragment_signin, container,
				false);
		
		SmartTrailApplication app = (SmartTrailApplication) getActivity().getApplication();
//		final Button signinButton = (Button) view.findViewById(R.id.signIn);
//		signinButton.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// close the keyboard
//				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//				imm.hideSoftInputFromWindow(mUsernameEditText.getWindowToken(),
//						0);
//				mSigninTask = new SigninTask().execute();
//			}
//		});
//		signinButton.setTypeface(app.mTf);

//		final Button signupButton = (Button) view.findViewById(R.id.signUp);
//		signupButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				signinButton.setText(R.string.signUp);
//				signupButton.setVisibility(View.INVISIBLE);
//				// turn this on once BMA server implementation is complete.
////				if (false) {
////					Intent intent = new Intent(SigninFragment.this,
////							SignupActivity.class);
////
////					startActivity(intent);
////
////					SigninFragment.this.finish();
////				} else {
////					Toast.makeText(
////							SigninFragment.this,
////							"Hey sorry, new signups are not available just yet. Stay tuned.",
////							Toast.LENGTH_LONG).show();
////
////				}
//			}
//		});
//		signupButton.setTypeface(app.mTf);

//		TextView alreadyAMemberTextView = ((TextView) view.findViewById(R.id.notMember));
//		alreadyAMemberTextView.setTypeface(app.mTf);
		
		TextView signinDescriptionTextView = ((TextView) view.findViewById(R.id.signinDescription));
		signinDescriptionTextView.setTypeface(app.mTf);

		mUsernameEditText = ((EditText) view.findViewById(R.id.username));
		mUsernameEditText.setTypeface(app.mTf);
//		mPasswordEditText = ((EditText) view.findViewById(R.id.password));
//		mPasswordEditText.setTypeface(app.mTf);

		mUsernameEditText.setText(app.getUsername());
		mUsernameEditText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				// close the keyboard
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mUsernameEditText.getWindowToken(),
						0);
				mSigninTask = new SigninTask().execute();
				return true;
			}});
//		mPasswordEditText.setText(app.getPassword());
//		if (app.getUsername() != null) {
//			mPasswordEditText.requestFocus();
//		}

//		final ImageButton showPassword = (ImageButton) view.findViewById(R.id.showPassword);
//		showPassword.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				mShow = !mShow;
//				int position = mPasswordEditText.getSelectionStart();
//				if (mShow) {
//					mPasswordEditText
//							.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
//					mPasswordEditText
//							.setTransformationMethod(new SingleLineTransformationMethod());
//					Editable etext = (Editable) mPasswordEditText.getText();
//					Selection.setSelection(etext, position);
//				} else {
//					mPasswordEditText
//							.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
//					mPasswordEditText
//							.setTransformationMethod(new PasswordTransformationMethod());
//					Editable etext = (Editable) mPasswordEditText.getText();
//					Selection.setSelection(etext, position);
//				}
//
//			}
//		});
		
		return view;
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

	private ProgressDialog showProgressDialog() {
		if (mProgressDialog == null) {
			ProgressDialog dialog = new ProgressDialog(getActivity());
			dialog.setTitle(R.string.login_dialog_title);
			dialog.setMessage(getString(R.string.login_dialog_message));
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			mProgressDialog = dialog;
		}
		mProgressDialog.show();
		return mProgressDialog;
	}

	private void dismissProgressDialog() {
		try {
			mProgressDialog.dismiss();
		} catch (IllegalArgumentException e) {
			// We don't mind. android cleared it for us.
		}
	}

//	@Override
//	public Object onRetainNonConfigurationInstance() {
//
//		if (mSigninTask != null) {
//			mSigninTask.cancel(true);
//		}
//		return mSigninTask;
//	}



	/**
	 * 
	 * @author Matt Tucker (matt@geozen.com)
	 * 
	 */
	private class SigninTask extends AsyncTask<Void, Void, Boolean> {

		private final String CLASSTAG = "SigninTask";

		private Exception mReason;

		@Override
		protected void onPreExecute() {

			showProgressDialog();
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			SmartTrailApplication app = (SmartTrailApplication) getActivity().getApplication();

			try {
				String email = mUsernameEditText.getText().toString();
				boolean signedin = app.signin(email);
				return signedin;
//				return true;

			} catch (Exception e) {
				AppLog.d(CLASSTAG, "Caught Exception logging in.", e);
				mReason = e;
				app.signoutUser();

				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean signedIn) {
			AppLog.d(CLASSTAG, "onPostExecute(): " + signedIn);

			if (signedIn) {

				//sendBroadcast(new Intent(Constants.INTENT_ACTION_LOGGED_IN));

				// Be done with the activity.
				getActivity().onBackPressed();

			} else {
				//sendBroadcast(new Intent(Constants.INTENT_ACTION_LOGGED_OUT));
				NotificationsUtil.ToastReasonForFailure(getActivity(),
						mReason);
			}
			dismissProgressDialog();
		}

		@Override
		protected void onCancelled() {
			dismissProgressDialog();
		}
	}

}