package org.bouldermountainbike.smarttrail.ui;

import java.util.ArrayList;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import org.bouldermountainbike.smarttrail.app.SmartTrailApplication;
import org.bouldermountainbike.smarttrail.model.MapData;
import org.bouldermountainbike.smarttrail.service.SyncService;
import org.bouldermountainbike.smarttrail.util.DetachableResultReceiver;
//import android.support.v4.app.Fragment;

/**
 * A non-UI fragment, retained across configuration changes, that updates its
 * listeners when sync status changes.
 */
public class SyncStatusUpdaterFragment extends Fragment implements
		DetachableResultReceiver.Receiver {
	public static final String TAG = SyncStatusUpdaterFragment.class.getName();

	DetachableResultReceiver mReceiver;
	ArrayList<SyncStatusListener> mListeners;

	public SyncStatusUpdaterFragment() {
		super();
		mReceiver = new DetachableResultReceiver(new Handler());
		mReceiver.setReceiver(this);
		mListeners = new ArrayList<SyncStatusListener>();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);

	}

	/** {@inheritDoc} */
	public void onReceiveResult(int resultCode, Bundle resultData) {

		updateListeners(resultCode);

		if (resultCode == SyncService.STATUS_FINISHED) {
			new UpdateMapDataTask().execute();
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	void registerListener(SyncStatusListener listener) {
		mListeners.add(listener);
	}

	void unregisterListener(SyncStatusListener listener) {
		mListeners.remove(listener);
	}

	void updateListeners(int status) {
		for (SyncStatusListener listener : mListeners) {
			listener.onStatusChanged(status);
		}
	}

	/**
	 * Populate the map data from the database.
	 * 
	 */
	private class UpdateMapDataTask extends AsyncTask<Void, Void, Boolean> {

		

		@Override
		protected Boolean doInBackground(Void... params) {
//			SmartTrailApplication app = (SmartTrailApplication) getActivity()
//					.getApplication();
//			MapData mapData = new MapData(app.getRegion());
//			mapData.readTrailData(getActivity());
//			mapData.readAreaData(getActivity().getContentResolver(),
//					app.getArea());
//			mapData.readPoiData(getActivity());
//			app.mMapData = mapData;

			return true;
		}

		@Override
		protected void onPostExecute(Boolean success) {

			updateListeners(SyncService.STATUS_MAPDATA_UPDATED);
		}

		@Override
		protected void onCancelled() {

		}

	}
}