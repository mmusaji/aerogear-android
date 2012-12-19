/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.aerogear.android.impl.datamanager;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import java.util.HashMap;
import java.util.Map;

/**
 * This ContentProvider can create tables and access data provided from a 
 * ContentProviderStore or ContentProviderPipe
 */
public class AeroGearContentProvider extends ContentProvider {

    private final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     * in the format of vnd.android.cursor.item/vnd.org.aerogear.provider.appName.klassName
     */
    private final Map<Uri, String> mimeTypeMap = new HashMap<Uri, String>();
    private String appName;
    private static final int AG_CODE = 0x00001;

    @Override
    public boolean onCreate() {
        //Do nothing?
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (matcher.match(uri)) {
        case UriMatcher.NO_MATCH:
            return null;
        }
        return null;

    }

    @Override
    public String getType(Uri uri) {
        return mimeTypeMap.get(uri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (matcher.match(uri)) {
        case UriMatcher.NO_MATCH:
            return null;
        }
        return null;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (matcher.match(uri)) {
        case UriMatcher.NO_MATCH:
            return 0;
        }
        return 0;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (matcher.match(uri)) {
        case UriMatcher.NO_MATCH:
            return 0;
        }
        return 0;
    }

    private String buildMimeType(Class klass) {
        StringBuilder typeBuilder = new StringBuilder();

        typeBuilder.append("vnd.android.cursor.item/vnd.org.aerogear.provider.")
                   .append(appName).append(".").append(klass.getSimpleName());
        return typeBuilder.toString();
    }

    private Uri buildUri(Class klass) {
        StringBuilder uriBuilder = new StringBuilder();

        uriBuilder.append("content://org.android.store")
                   .append(appName).append("/").append(klass.getSimpleName());

        return Uri.parse(uriBuilder.toString());
    }

    public void addType(Class klass) {
        Uri local = buildUri(klass);
        String mimeType = buildMimeType(klass);
        matcher.addURI(appName, local.getPath(), AG_CODE);
        mimeTypeMap.put(local, mimeType);
    }

}
