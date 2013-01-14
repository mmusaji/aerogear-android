/*
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
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
package org.jboss.aerogear.android.impl.pipeline;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.Provider;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.authentication.AuthorizationFields;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.http.HttpProvider;
import org.jboss.aerogear.android.impl.core.HttpProviderFactory;
import org.jboss.aerogear.android.impl.reflection.Property;
import org.jboss.aerogear.android.impl.reflection.Scan;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.PipeType;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Rest implementation of {@link Pipe}.
 */
public final class RestAdapter<T> implements Pipe<T> {

    private static final String TAG = RestAdapter.class.getSimpleName();
    private static final String UTF_8 = "UTF-8";
    private final Gson gson;
    /**
     * A class of the Generic type this pipe wraps. This is used by GSON for
     * deserializing.
     */
    private final Class<T> klass;
    /**
     * A class of the Generic collection type this pipe wraps. This is used by
     * JSON for deserializing collections.
     */
    private final Class<T[]> arrayKlass;
    private final URL baseURL;
    private final Provider<HttpProvider> httpProviderFactory = new HttpProviderFactory();
    private AuthenticationModule authModule;
    private Charset encoding = Charset.forName("UTF-8");

    public RestAdapter(Class<T> klass, URL baseURL) {
        this.klass = klass;
        this.arrayKlass = asArrayClass(klass);
        this.baseURL = baseURL;
        this.gson = new Gson();
    }

    public RestAdapter(Class<T> klass, URL baseURL,
            GsonBuilder gsonBuilder) {
        this.klass = klass;
        this.arrayKlass = asArrayClass(klass);
        this.baseURL = baseURL;
        this.gson = gsonBuilder.create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PipeType getType() {
        return PipeTypes.REST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getUrl() {
        return baseURL;
    }

    @Override
    public void readWithFilter(ReadFilter filter, final Callback<List<T>> callback) {
        if (filter == null) {
            filter = new ReadFilter();
        }
        final ReadFilter innerFilter = filter;

        new AsyncTask<Void, Void, Void>() {
            List<T> result = null;
            Exception exception = null;

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    HttpProvider httpProvider = getHttpProvider(URLDecoder.decode(innerFilter.getQuery(), UTF_8));
                    byte[] responseBody = httpProvider.get().getBody();
                    String responseAsString = new String(responseBody, encoding);
                    JsonParser parser = new JsonParser();
                    JsonElement result = parser.parse(responseAsString);
                    if (result.isJsonArray()) {
                        T[] resultArray = gson.fromJson(responseAsString, arrayKlass);
                        this.result = Arrays.asList(resultArray);
                    } else {
                        T resultObject = gson.fromJson(responseAsString, klass);
                        List<T> resultList = new ArrayList<T>(1);
                        resultList.add(resultObject);
                        this.result = resultList;
                    }
                } catch (Exception e) {
                    exception = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void ignore) {
                super.onPostExecute(ignore);
                if (exception == null) {
                    callback.onSuccess(this.result);
                } else {
                    callback.onFailure(exception);
                }
            }
        }.execute();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(final Callback<List<T>> callback) {
        readWithFilter(null, callback);
    }

    @Override
    public void save(final T data, final Callback<T> callback) {
        final String id;

        try {
            String recordIdFieldName = Scan.recordIdFieldNameIn(data.getClass());
            Object result = new Property(data.getClass(), recordIdFieldName).getValue(data);
            id = result == null ? null : result.toString();
        } catch (Exception e) {
            callback.onFailure(e);
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            T result = null;
            Exception exception = null;

            @Override
            protected Void doInBackground(Void... voids) {
                try {

                    String body = gson.toJson(data);
                    final HttpProvider httpProvider = getHttpProvider();

                    HeaderAndBody result;
                    if (id == null || id.length() == 0) {
                        result = httpProvider.post(body);
                    } else {
                        result = httpProvider.put(id, body);
                    }

                    this.result = gson.fromJson(new String(result.getBody(), encoding), klass);

                } catch (Exception e) {
                    exception = e;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void ignore) {
                super.onPostExecute(ignore);
                if (exception == null) {
                    callback.onSuccess(this.result);
                } else {
                    callback.onFailure(exception);
                }
            }
        }.execute();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final String id, final Callback<Void> callback) {

        new AsyncTask<Void, Void, Void>() {
            Exception exception = null;

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    HttpProvider httpProvider = getHttpProvider();
                    httpProvider.delete(id);
                } catch (Exception e) {
                    exception = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void ignore) {
                super.onPostExecute(ignore);
                if (exception == null) {
                    callback.onSuccess(null);
                } else {
                    callback.onFailure(exception);
                }
            }
        }.execute();

    }

    /**
     * This will return a class of the type T[] from a given class. When we read
     * from the AG pipe, Java needs a reference to a generic array type.
     *
     * @param klass
     * @return an array of klass with a length of 1
     */
    private Class<T[]> asArrayClass(Class<T> klass) {

        return (Class<T[]>) Array.newInstance(klass, 1).getClass();
    }

    private URL appendQuery(String query, URL baseURL) {
        try {
            URI baseURI = baseURL.toURI();
            String baseQuery = baseURI.getQuery();
            if (baseQuery == null || baseQuery.isEmpty()) {
                baseQuery = query;
            } else {
                baseQuery = baseQuery + "&" + query;
            }

            return new URI(baseURI.getScheme(), baseURI.getUserInfo(), baseURI.getHost(), baseURI.getPort(), baseURI.getPath(), baseQuery, baseURI.getFragment()).toURL();
        } catch (MalformedURLException ex) {
            Log.e(TAG, "The URL could not be created from " + baseURL.toString(), ex);
            throw new RuntimeException(ex);
        } catch (URISyntaxException ex) {
            Log.e(TAG, "Error turning " + query + " into URI query.", ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void setAuthenticationModule(AuthenticationModule module) {
        this.authModule = module;
    }

    /**
     * Apply authentication if the token is present
     */
    private AuthorizationFields loadAuth() {

        if (authModule != null && authModule.isLoggedIn()) {
            return authModule.getAuthorizationFields();
        }

        return new AuthorizationFields();
    }

    /**
     * Sets the encoding of the Pipe. May not be null.
     *
     * @param encoding
     * @throws IllegalArgumentException if encoding is null
     */
    public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }

    /**
     *
     * @param queryParameters
     * @return a url with query params added
     */
    private URL addAuthorization(List<Pair<String, String>> queryParameters) {

        StringBuilder queryBuilder = new StringBuilder();

        String amp = "";
        for (Pair<String, String> parameter : queryParameters) {
            try {
                queryBuilder.append(amp)
                            .append(URLEncoder.encode(parameter.first, "UTF-8"))
                            .append("=")
                            .append(URLEncoder.encode(parameter.second, "UTF-8"));

                amp = "&";
            } catch (UnsupportedEncodingException ex) {
                Log.e(TAG, "UTF-8 encoding is not supported.", ex);
                throw new RuntimeException(ex);

            }
        }

        return appendQuery(queryBuilder.toString(), baseURL);

    }

    private void addAuthHeaders(HttpProvider httpProvider, AuthorizationFields fields) {
        List<Pair<String, String>> authHeaders = fields.getHeaders();

        for (Pair<String, String> header : authHeaders) {
            httpProvider.setDefaultHeader(header.first, header.second);
        }

    }

    private HttpProvider getHttpProvider() {
        return getHttpProvider(null);
    }

    private HttpProvider getHttpProvider(String filterQuery) {
        AuthorizationFields fields = loadAuth();
        URL authorizedURL = addAuthorization(fields.getQueryParameters());
        if (!(filterQuery == null || filterQuery.isEmpty())) {
            authorizedURL = appendQuery(filterQuery, authorizedURL);
        }
        final HttpProvider httpProvider = httpProviderFactory.get(authorizedURL);
        addAuthHeaders(httpProvider, fields);
        return httpProvider;
    }
}
