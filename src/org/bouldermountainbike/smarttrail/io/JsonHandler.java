/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package org.bouldermountainbike.smarttrail.io;

import java.io.IOException;

import org.json.JSONException;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;

/**
 * Abstract class that handles reading and parsing an {@link JsonParser} into a
 * set of {@link ContentProviderOperation}. It catches recoverable network
 * exceptions and rethrows them as {@link HandlerException}. Any local
 * {@link ContentProvider} exceptions are considered unrecoverable.
 * <p>
 * This class is only designed to handle simple one-way synchronization.
 */
public abstract class JsonHandler {

	/**
	 * Parse the given {@link JsonParser}, turning into a series of
	 * {@link ContentProviderOperation} that are immediately applied using the
	 * given {@link ContentResolver}.
	 * 
	 * @throws JSONException
	 */
	public abstract void parseAndApply(String json, ContentResolver resolver)
			throws JSONException;

	/**
	 * General {@link IOException} that indicates a problem occured while
	 * parsing.
	 */
	public static class HandlerException extends IOException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public HandlerException(String message) {
			super(message);
		}

		public HandlerException(String message, Throwable cause) {
			super(message);
			initCause(cause);
		}

		@Override
		public String toString() {
			if (getCause() != null) {
				return getLocalizedMessage() + ": " + getCause();
			} else {
				return getLocalizedMessage();
			}
		}
	}
}
