/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package org.bouldermountainbike.smarttrail.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONException;

import android.content.ContentResolver;
import android.content.Context;

/**
 * Opens a local asset and passes the resulting string to a {@link JsonHandler}.
 */
public class LocalExecutor {
    private ContentResolver mResolver;

    public LocalExecutor(ContentResolver resolver) {
        mResolver = resolver;
    }

    public void execute(Context context, String assetName, JsonHandler handler)
            throws JSONException, IOException {
            
        	InputStream inputStream = context.getAssets().open(assetName);
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }

            String sjson = total.toString();
            handler.parseAndApply(sjson, mResolver);
   
    }

   
}
