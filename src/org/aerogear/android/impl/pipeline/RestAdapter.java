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

package org.aerogear.android.impl.pipeline;

import android.os.AsyncTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.aerogear.android.Callback;
import org.aerogear.android.authentication.AuthenticationModule;
import org.aerogear.android.core.HeaderAndBody;
import org.aerogear.android.core.HttpProvider;
import org.aerogear.android.pipeline.Pipe;
import org.aerogear.android.pipeline.PipeType;

/**
 * Rest implementation of {@link Pipe}.
 */
public final class RestAdapter<T> implements Pipe<T> {

    private final Gson gson;

    /**
     * A class of the Generic type this pipe wraps.
     * This is used by GSON for deserializing.
     */
    private final Class<T> klass;

    /**
     * A class of the Generic collection type this pipe wraps.
     * This is used by JSON for deserializing collections.
     */
    private final Class<T[]> arrayKlass;


    private final HttpProvider httpProvider;
    private AuthenticationModule authModule;
    private static final String TAG = "RestAdapter";

    private Charset encoding = Charset.forName("UTF-8");
    
    public RestAdapter(Class<T> klass, HttpProvider httpProvider) {
        this.klass = klass;
        this.arrayKlass = asArrayClass(klass);
        this.httpProvider = httpProvider;
        this.gson = new Gson();
    }

    public RestAdapter(Class<T> klass, HttpProvider httpProvider, GsonBuilder gsonBuilder) {
        this.klass = klass;
        this.arrayKlass = asArrayClass(klass);
        this.httpProvider = httpProvider;
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
        return httpProvider.getUrl();
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public void read(final Callback<List<T>> callback) {
        new AsyncTask<Void, Void, AsyncTaskResult<List<T>>>() {
            @Override
            protected AsyncTaskResult doInBackground(Void... voids) {
                try {
                    applyAuthToken();
                    byte[] responseBody = httpProvider.get().getBody();
                    String responseAsString = new String(responseBody, encoding);
                    JsonParser parser = new JsonParser();
                    JsonElement result = parser.parse(responseAsString);
                    if (result.isJsonArray()) {
                    	T[] resultArray = gson.fromJson(responseAsString, arrayKlass);
                    	return new AsyncTaskResult(Arrays.asList(resultArray));
                    } else {
                    	T resultObject = gson.fromJson(responseAsString, klass);
                    	List<T> resultList = new ArrayList<T>(1);
                    	resultList.add(resultObject);
                    	return new AsyncTaskResult(resultList);
                    	
                    }
                } catch (Exception e) {
                    return new AsyncTaskResult(e);
                }
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<List<T>> asyncTaskResult) {
                if (asyncTaskResult.getError() != null) {
                    callback.onFailure(asyncTaskResult.getError());
                } else {
                    callback.onSuccess(asyncTaskResult.getResult());
                }
            }
        }.execute();
    }


    @Override
    public void save(final T data, final Callback<T> callback) {

        final String id;

        // TODO: Make "id" field configurable
        try {
            Method idGetter = data.getClass().getMethod("getId");
            Object result = idGetter.invoke(data);
            id = result == null ? null : result.toString();
        } catch (Exception e) {
            callback.onFailure(e);
            return;
        }

        new AsyncTask<Void, Void, AsyncTaskResult<T>>() {
            @Override
            protected AsyncTaskResult doInBackground(Void... voids) {
                try {

                    /*Serialize the object.*/
                    String body = gson.toJson(data);
                    applyAuthToken();


                    HeaderAndBody result = null;
                    if (id == null || id.length() == 0) {
                        result = httpProvider.post(body);
                    } else {
                        result = httpProvider.put(id, body);
                    }

                    /*Deseralize the result and return it, or pass null.*/

                    if (result != null) {
                        return new AsyncTaskResult(gson.fromJson(new String(result.getBody(), encoding), klass));
                    } else {
                        return new AsyncTaskResult((T) null);

                    }

                } catch (Exception e) {
                    return new AsyncTaskResult(e);
                }
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<T> asyncTaskResult) {
                if (asyncTaskResult.getError() != null) {
                    callback.onFailure(asyncTaskResult.getError());
                } else {
                    callback.onSuccess(asyncTaskResult.getResult());
                }
            }
        }.execute();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final String id, final Callback<Void> callback) {
        new AsyncTask<Void, Void, AsyncTaskResult<byte[]>>() {
            @Override
            protected AsyncTaskResult doInBackground(Void... voids) {
                try {
                    applyAuthToken();
                    return new AsyncTaskResult(httpProvider.delete(id));
                } catch (Exception e) {
                    return new AsyncTaskResult(e);
                }
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<byte[]> asyncTaskResult) {
                if (asyncTaskResult.getError() != null) {
                    callback.onFailure(asyncTaskResult.getError());
                } else {
                    callback.onSuccess(null);
                }
            }
        }.execute();
    }


    /**
     * This will return a class of the type T[] from a given class.
     * When we read from the AG pipe, Java needs a reference to a
     * generic array type.
     *
     * @param klass
     * @return
     */
    private Class<T[]> asArrayClass(Class<T> klass) {
        return (Class<T[]>) ((T[]) Array.newInstance(klass, 1)).getClass();
    }

    private class AsyncTaskResult<T> {

        private T result;
        private Exception error;

        public AsyncTaskResult(T result) {
            this.result = result;
        }

        public AsyncTaskResult(Exception error) {
            this.error = error;
        }

        public T getResult() {
            return result;
        }

        public Exception getError() {
            return error;
        }

    }

    @Override
    public void setAuthenticationModule(AuthenticationModule module) {
        this.authModule = module;
    }

    /**
     * Apply authentication if the token is present
     */
    private void applyAuthToken() {
        if (authModule != null && authModule.isLoggedIn()) {
            authModule.onSecurityApplicationRequested(httpProvider);
        }
    }

}
