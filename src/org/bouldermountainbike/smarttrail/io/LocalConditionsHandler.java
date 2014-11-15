/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package org.bouldermountainbike.smarttrail.io;

import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.END_TAG;
import static org.xmlpull.v1.XmlPullParser.START_TAG;
import static org.xmlpull.v1.XmlPullParser.TEXT;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;

import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.ConditionsColumns;
import org.bouldermountainbike.smarttrail.provider.SmartTrailSchema.ConditionsSchema;
import org.bouldermountainbike.smarttrail.util.Lists;

public class LocalConditionsHandler extends XmlHandler {

	public LocalConditionsHandler() {
		super(SmartTrailSchema.CONTENT_AUTHORITY);
	}

	@Override
	public ArrayList<ContentProviderOperation> parse(XmlPullParser parser,
			ContentResolver resolver) throws XmlPullParserException,
			IOException {
		final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();

		int type;
		while ((type = parser.next()) != END_DOCUMENT) {
			if (type == START_TAG && Tags.CONDITION.equals(parser.getName())) {
				parseCondition(parser, batch, resolver);
			}
		}

		return batch;
	}

	private static void parseCondition(XmlPullParser parser,
			ArrayList<ContentProviderOperation> batch, ContentResolver resolver)
			throws XmlPullParserException, IOException {
		final int depth = parser.getDepth();
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newInsert(ConditionsSchema.CONTENT_URI);

		String tag = null;
		int type;
		while (((type = parser.next()) != END_TAG || parser.getDepth() > depth)
				&& type != END_DOCUMENT) {
			if (type == START_TAG) {
				tag = parser.getName();
			} else if (type == END_TAG) {
				tag = null;
			} else if (type == TEXT) {
				final String text = parser.getText();
				if (ConditionsColumns.CONDITION_ID.equals(tag)) {
					builder.withValue(ConditionsColumns.CONDITION_ID, text);
				} else if (ConditionsColumns.TRAIL_ID.equals(tag)) {
					builder.withValue(ConditionsColumns.TRAIL_ID, text);
				} else if (ConditionsColumns.USERNAME.equals(tag)) {
					builder.withValue(ConditionsColumns.USERNAME, text);
				} else if (ConditionsColumns.USER_TYPE.equals(tag)) {
					builder.withValue(ConditionsColumns.USER_TYPE, text);
				} else if (ConditionsColumns.CONDITION.equals(tag)) {
					builder.withValue(ConditionsColumns.CONDITION, text);
				} else if (ConditionsColumns.COMMENT.equals(tag)) {
					builder.withValue(ConditionsColumns.COMMENT, text);
				} else if (ConditionsColumns.UPDATED_AT.equals(tag)) {
					builder.withValue(ConditionsColumns.UPDATED_AT,
							Long.parseLong(text));
				}
			}
		}

		batch.add(builder.build());

	}

	interface Tags {
		String CONDITION = "condition";
	}

}
