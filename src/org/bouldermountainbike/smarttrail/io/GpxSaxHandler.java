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

import com.google.android.gms.maps.model.LatLng;

public class GpxSaxHandler extends DefaultHandler {

	// ===========================================================
	// Fields
	// ===========================================================


	private StringBuffer buf = new StringBuffer();
	private double lat;
	private double lon;
	@SuppressWarnings("unused")
	private double ele;
	private List<LatLng> track;

	// ===========================================================
	// Methods
	// ===========================================================
	@Override
	public void startDocument() throws SAXException {
		track = new ArrayList<LatLng>();
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
			String qName, Attributes attributes) throws SAXException {
		buf.setLength(0);
		if (qName.equals("trkpt")) {
			lat = Double.parseDouble(attributes.getValue("lat"));
			lon = Double.parseDouble(attributes.getValue("lon"));
		}

	}

	/**
	 * Gets be called on closing tags like: </tag>
	 */
	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		if (qName.equals("trkpt")) {
			// track.add(Trackpoint.fromWGS84(lat, lon, ele, time));
			LatLng point = new LatLng(lat, lon);
			track.add(point);
		} else if (qName.equals("ele")) {
			ele = Double.parseDouble(buf.toString());
		}
	}

	/**
	 * Gets be called on the following structure: <tag>characters</tag>
	 */
	@Override
	public void characters(char[] chars, int start, int length) {
		buf.append(chars, start, length);
	}
	
	
	public List<LatLng> getPoints() {
		return track;
	}
}