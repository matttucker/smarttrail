package org.bouldermountainbike.smarttrail.ui;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import org.bouldermountainbike.smarttrail.R;
import org.bouldermountainbike.smarttrail.app.SmartTrailApplication;
import org.bouldermountainbike.smarttrail.service.SyncService;
//import com.slidingmenu.lib.SlidingMenu;
//import com.slidingmenu.lib.app.SlidingFragmentActivity;

//public class FragmentChangeActivity extends SlidingFragmentActivity {
//
//	private int mTitleRes;
//	private SlidingMenu mSlidingMenu;
//	SyncStatusUpdaterFragment mSyncStatusUpdaterFragment;
//	private static String mMainFragmentTag;
//
//	public FragmentChangeActivity() {
//		mTitleRes = R.string.app_name;
//	}
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//
//		setTitle(mTitleRes);
//
//		// set the Behind View
//		setBehindContentView(R.layout.fragment_slidingmenu);
//
//		// customize the SlidingMenu
//		mSlidingMenu = getSlidingMenu();
//		mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
//		mSlidingMenu.setShadowDrawable(R.drawable.shadow);
//		mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
//		mSlidingMenu.setFadeDegree(0.35f);
//		mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
//
//		try {
//			ActionBar actionBar = getActionBar();
//			actionBar.setDisplayHomeAsUpEnabled(true);
//			actionBar.setTitle("");
//			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
//		} catch (Exception ex) {
//
//		}
//
//		final int titleId = Resources.getSystem().getIdentifier(
//				"action_bar_title", "id", "android");
//		TextView title = (TextView) getWindow().findViewById(titleId);
//		SmartTrailApplication app = (SmartTrailApplication) getApplication();
//		title.setTypeface(app.mTf);
//
//		FragmentManager fm = getFragmentManager();
//		// set the Above View
//		Fragment fragment = null;
//		if (savedInstanceState != null)
//			fragment = fm.getFragment(savedInstanceState, "mContent");
//		if (fragment == null) {
//			fragment = TrailMapFragment.getInstance();
//			mMainFragmentTag = "trailMapFragment";
//		}
//
//		// set the Above View
//		setContentView(R.layout.content_frame);
//		fm.beginTransaction().replace(R.id.content_frame, fragment).commit();
//
//		// set the Behind View
//		setBehindContentView(R.layout.fragment_slidingmenu);
//		fm.beginTransaction()
//				.replace(R.id.menu_frame, new SlidingMenuFragment()).commit();
//
//		mSyncStatusUpdaterFragment = (SyncStatusUpdaterFragment) fm
//				.findFragmentByTag(SyncStatusUpdaterFragment.TAG);
//		if (mSyncStatusUpdaterFragment == null) {
//			mSyncStatusUpdaterFragment = new SyncStatusUpdaterFragment();
//			fm.beginTransaction()
//					.add(mSyncStatusUpdaterFragment,
//							SyncStatusUpdaterFragment.TAG).commit();
//			triggerRefresh();
//
//		}
//
//	}
//
//	@Override
//	public void onSaveInstanceState(Bundle outState) {
//		super.onSaveInstanceState(outState);
//
//		int n = getFragmentManager().getBackStackEntryCount();
//		Fragment fragment = null;
//		if (n > 0) {
//			FragmentManager.BackStackEntry backEntry = getFragmentManager()
//					.getBackStackEntryAt(n - 1);
//			String str = backEntry.getName();
//			fragment = getFragmentManager().findFragmentByTag(str);
//		}
//		if (fragment == null) {
//			fragment = getFragmentManager().findFragmentByTag(mMainFragmentTag);
//		}
//
//		if (fragment != null)
//			getFragmentManager().putFragment(outState, "mContent", fragment);
//	}
//
//	public void switchMainContent(Fragment fragment, String tag) {
//		FragmentManager fm = getFragmentManager();
//		mMainFragmentTag = tag;
//
//		fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//		fm.beginTransaction().replace(R.id.content_frame, fragment, tag)
//				.commit();
//		getSlidingMenu().showContent();
//
//	}
//
//	public void switchSubContent(Fragment fragment, String name) {
//		FragmentManager fm = getFragmentManager();
//
//		fm.beginTransaction().replace(R.id.content_frame, fragment, name)
//				.addToBackStack(name).commit();
//	}
//
//	public boolean onOptionsItemSelected(MenuItem item) {
//
//		// Intent intent;
//		switch (item.getItemId()) {
//
//		default:
//			return super.onOptionsItemSelected(item);
//
//		}
//
//	}
//
//	void toggleSlidingMenu() {
//		mSlidingMenu.toggle();
//	}
//
//	protected void triggerRefresh() {
//		final Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
//				SyncService.class);
//		intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER,
//				mSyncStatusUpdaterFragment.mReceiver);
//		startService(intent);
//
//	}
//
//}
