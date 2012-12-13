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
package org.aerogear.android;

import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.json.JSONObject;

/**
 * This class wraps and builds the query parameters for filtering and pagination
 */
public class ReadFilter {

    private static final String UTF_8 = "UTF-8";
    private static final String TAG = ReadFilter.class.getSimpleName();

    private Integer limit = Integer.MAX_VALUE;
    private Integer offset = 0;
    private Integer perPage = Integer.MAX_VALUE;
    private JSONObject where = new JSONObject();

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getPerPage() {
        return perPage;
    }

    public void setPerPage(Integer perPage) {
        this.perPage = perPage;
    }

    public JSONObject getWhere() {
        return where;
    }

    public void setWhere(JSONObject where) {
        this.where = where;
    }

    /**
     * 
     * @return a URL encoded query which represents the values set in this object.
     */
    public String getQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        String amp = "";

        if (limit != null && limit != Integer.MAX_VALUE) {
            queryBuilder.append(amp).append("limit=").append(limit);
            amp = "&";
        }

        if (offset != null && offset > 0) {
            queryBuilder.append(amp).append("offset=").append(offset);
            amp = "&";
        }

        if (perPage != null && perPage != Integer.MAX_VALUE) {
            queryBuilder.append(amp).append("per_page=").append(perPage);
            amp = "&";
        }

        if (where != null && where.length() > 0) {
            try {
                queryBuilder.append(amp).append("where=").append(URLEncoder.encode(where.toString(), UTF_8).replace("%3A", ":"));//The encoder shouldn't encode colons.
            } catch (UnsupportedEncodingException ex) {
                Log.e(TAG, "UTF-8 isn't supported on this platform", ex);
                throw new RuntimeException(ex);
            }
            amp = "&";
        }

        return queryBuilder.toString();
    }

}
