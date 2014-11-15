/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package org.bouldermountainbike.smarttrail.io;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.bouldermountainbike.smarttrail.util.AppLog;
import com.google.android.gms.maps.model.LatLng;

public class TrailSaxHandler extends DefaultHandler {

	// ===========================================================
	// Fields
	// ===========================================================

	@SuppressWarnings("unused")
	private boolean in_kmltag = false;
	@SuppressWarnings("unused")
	private boolean in_placemarktag = false;
	private boolean in_nametag = false;
	private boolean in_descriptiontag = false;
	@SuppressWarnings("unused")
	private boolean in_geometrycollectiontag = false;
	@SuppressWarnings("unused")
	private boolean in_pointtag = false;
	private boolean in_coordinatestag = false;

	private boolean mInGpxCoordinateTag;
	@SuppressWarnings("unused")
	private boolean mInGpxTrackTag;

	public List<LatLng> mPoints;

	private StringBuffer mBuffer;

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
		mPoints = new ArrayList<LatLng>();
	}

	@Override
	public void endDocument() throws SAXException {
		// Nothing to do
	}

	/**
	 * Gets be called on opening tags like: <tag> Can provide attribute(s), when
	 * xml was like: <tag attribute="attributeValue">
	 */
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {

		if (localName.equals("kml")) {
			this.in_kmltag = true;
		} else if (localName.equals("Placemark")) {
			this.in_placemarktag = true;
		} else if (localName.equals("name")) {
			this.in_nametag = true;
		} else if (localName.equals("description")) {
			this.in_descriptiontag = true;
		} else if (localName.equals("GeometryCollection")) {
			this.in_geometrycollectiontag = true;
		} else if (localName.equals("LineString")) {
		} else if (localName.equals("point")) {
			this.in_pointtag = true;
		} else if (localName.equals("coordinates")) {
			mBuffer = new StringBuffer(1024);
			this.in_coordinatestag = true;
		} else if (qName.equals("gx:coord")) {
			this.mInGpxCoordinateTag = true;
		} else if (qName.equals("gx:Track")) {
			mBuffer = new StringBuffer(1024);
			this.mInGpxTrackTag = true;
		}
	}

	/**
	 * Gets be called on closing tags like: </tag>
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		if (localName.equals("kml")) {
			this.in_kmltag = false;

		} else if (localName.equals("Placemark")) {

			this.in_placemarktag = false;
			String coordinates;
			if (mBuffer != null) {
				coordinates = mBuffer.toString().trim();
			} else {
				coordinates = null;
			}
			pathToPoints(coordinates);

		} else if (localName.equals("name")) {
			this.in_nametag = false;
		} else if (localName.equals("description")) {
			this.in_descriptiontag = false;
		} else if (localName.equals("GeometryCollection")) {
			this.in_geometrycollectiontag = false;
		} else if (localName.equals("LineString")) {
		} else if (localName.equals("point")) {
			this.in_pointtag = false;
		} else if (localName.equals("coordinates")) {
			this.in_coordinatestag = false;
		} else if (qName.equals("gx:coord")) {
			this.mInGpxCoordinateTag = false;
		} else if (qName.equals("gx:Track")) {
			this.mInGpxTrackTag = false;
		}
	}

	private void pathToPoints(String path) {

		if (path == null) {
			return;
		}
		// coordinates are in the form of
		// lat1,long1,elevation1 lat2,long2,elevation2 ...

		LatLng point;

		path = path.trim();

		String[] coords = path.trim().split(" ");
		String[] lngLatElev;
		double lon;
		double lat;
		for (int i = 0; i < coords.length; i++) {

			lngLatElev = coords[i].split(",");
			if (lngLatElev.length == 3) {
				lon = Double.parseDouble(lngLatElev[0]);
				lat = Double.parseDouble(lngLatElev[1]);

				point = new LatLng(lat, lon);
				mPoints.add(point);
			} else {
				// weird parse.
				AppLog.w("Bad kml parse at index=" + i + ", data=" + coords[i]);
			}

		}
	}

	/**
	 * Gets be called on the following structure: <tag>characters</tag>
	 */
	@Override
	public void characters(char ch[], int start, int length) {
		if (this.in_nametag) {

			// mTrailDataSet.mTrailName = new String(ch, start, length);

		} else if (this.in_descriptiontag) {
			// mTrailDataSet.mDescription = new String(ch, start, length);

		} else if (this.in_coordinatestag) {
			if (length == 1)
				return;
			String str = new String(ch, start, length);
			str = str.trim() + " ";
			mBuffer.append(str);

		} else if (this.mInGpxCoordinateTag) {
			// convert from gpx format to google earth format
			String str = new String(ch, start, length);
			str = str.replace(" ", ",") + " ";
			mBuffer.append(str);
		}
	}
}