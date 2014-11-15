package org.bouldermountainbike.smarttrail.map;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.json.JSONException;

import android.content.Context;

import org.bouldermountainbike.smarttrail.R;
import org.bouldermountainbike.smarttrail.model.Condition;
import org.bouldermountainbike.smarttrail.model.Trail;
import org.bouldermountainbike.smarttrail.util.TimeUtil;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapTrail {
	public Trail mTrail;

	// Maps and Markers
	public PolylineOptions mMapLine;
	public List<MarkerOptions> mTrailheadMarkers;
	public MarkerOptions mDifficultyMarker;

	public Polyline mMapPolyline;

	public int mColor;


	public MapTrail(Context context, Trail trail, String areaName) {
		mTrail = trail;
		trail.mStatus=filterStatus(trail.mStatus, trail.mStatusUpdatedAt);
		mColor = Condition.getStatusColor(context, trail.mStatus);
		mMapLine = new PolylineOptions();
		try {
			List<LatLng> trailPoints = mTrail.getTrailPoints();
			mMapLine.width(4.0f).addAll(trailPoints)
					.color(mColor);

			int n = trailPoints.size();
			if (n > 0) {
				LatLng midpoint = trailPoints.get(n / 2);
				mDifficultyMarker = new MarkerOptions();
				mDifficultyMarker
						.position(midpoint)
						.title(mTrail.mName)
						.snippet(areaName)
						.anchor(0.5f, 0.5f)
						.icon(BitmapDescriptorFactory
								.fromResource(getDifficultyResouceId()));
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mTrailheadMarkers = new ArrayList<MarkerOptions>();
		try {
			List<LatLng> trailheadPoints = mTrail.getTrailHeadPoints();

			for (LatLng point : trailheadPoints) {
				MarkerOptions marker = new MarkerOptions();
				marker.position(point)
						.title(mTrail.mName)
						.snippet(areaName)
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.map_icon));
				mTrailheadMarkers.add(marker);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public int getDifficultyResouceId() {
		switch (mTrail.mDifficultyRating ) {
		default:
		case 1:
			return R.drawable.imba_easiest_small;
		case 2:
			return R.drawable.imba_easy_small;
		case 3:
			return R.drawable.imba_more_difficult_small;
		case 4:
			return R.drawable.imba_very_difficult_small;
		case 5:
			return R.drawable.imba_extremely_difficult_small;
		}
	}
	
	public String filterStatus(String status, long updatedAt) {
		long delta = new GregorianCalendar().getTimeInMillis() - updatedAt;
		if (delta > 3*TimeUtil.DAY_MS) {
			status = Condition.UNKNOWN;
		}
		return status;
	}
	public void setStatus(Context context, String status, long updatedAt) {
		
		mTrail.mStatus = status;
		mTrail.mStatusUpdatedAt = updatedAt;
		mColor = Condition.getStatusColor(context, filterStatus(status, updatedAt));
		mMapLine.color(mColor);

	}
}
