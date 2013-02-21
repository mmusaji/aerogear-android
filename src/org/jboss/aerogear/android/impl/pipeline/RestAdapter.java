/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and
 * individual contributors by the
 *
 * @authors tag. See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.jboss.aerogear.android.impl.pipeline;

import org.jboss.aerogear.android.impl.pipeline.paging.DefaultParameterProvider;
import org.jboss.aerogear.android.impl.pipeline.paging.URIPageHeaderParser;
import org.jboss.aerogear.android.impl.pipeline.paging.URIBodyPageParser;
import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.GsonBuilder;
import java.net.URL;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.PipeType;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.jboss.aerogear.android.pipeline.paging.PageConfig;
import org.jboss.aerogear.android.pipeline.paging.ParameterProvider;

/**
 * Rest implementation of {@link Pipe}.
 */
public final class RestAdapter<T> implements Pipe<T> {

    private static final String TAG = RestAdapter.class.getSimpleName();
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 64;
    private static final int KEEP_ALIVE = 1;
    private static final BlockingQueue<Runnable> WORK_QUEUE =
            new LinkedBlockingQueue<Runnable>(10);
    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, WORK_QUEUE);
    private ParameterProvider parameterProvider = new DefaultParameterProvider();
    /**
     * A class of the Generic type this pipe wraps. This is used by GSON for
     * deserializing.
     */
    private final Class<T> klass;
    /**
     * A class of the Generic collection type this pipe wraps. This is used by
     * JSON for deserializing collections.
     */
    private final URL baseURL;
    private final RestRunner<T> restRunner;

    public RestAdapter(Class<T> klass, URL baseURL) {
        this.restRunner = new RestRunner(klass, baseURL);
        this.klass = klass;

        this.baseURL = baseURL;
    }

    public RestAdapter(Class<T> klass, URL baseURL,
            GsonBuilder gsonBuilder) {
        this.restRunner = new RestRunner(klass, baseURL, gsonBuilder);
        this.klass = klass;

        this.baseURL = baseURL;
    }

    public RestAdapter(Class<T> klass, URL baseURL, PageConfig pageconfig) {
        this.restRunner = new RestRunner(klass, baseURL, pageconfig);
        this.klass = klass;

        this.baseURL = baseURL;
    }

    public RestAdapter(Class<T> klass, URL baseURL,
            GsonBuilder gsonBuilder, PageConfig pageconfig) {
        this.restRunner = new RestRunner(klass, baseURL, gsonBuilder, pageconfig);
        this.klass = klass;

        this.baseURL = baseURL;
        if (pageconfig != null) {
            if (pageconfig.getPageHeaderParser() == null) {
                if (PageConfig.MetadataLocations.BODY.equals(pageconfig.getMetadataLocation())) {
                    pageconfig.setPageHeaderParser(new URIBodyPageParser(baseURL));
                } else if (PageConfig.MetadataLocations.HEADERS.equals(pageconfig.getMetadataLocation())) {
                    pageconfig.setPageHeaderParser(new URIPageHeaderParser(baseURL));
                }
            }
        }
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

        THREAD_POOL_EXECUTOR.execute(new Runnable() {
            List<T> result = null;
            Exception exception = null;

            @Override
            public void run() {
                try {
                    this.result = restRunner.readWithFilter(innerFilter, RestAdapter.this);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    this.exception = e;
                }
                if (exception == null) {
                    callback.onSuccess(this.result);
                } else {
                    callback.onFailure(exception);
                }
            }
        });

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

        THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                T result = null;
                Exception exception = null;

                try {
                    result = restRunner.save(data);
                } catch (Exception e) {
                    exception = e;
                }

                if (exception == null) {
                    callback.onSuccess(result);
                } else {
                    callback.onFailure(exception);
                }
            }
        });


    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final String id, final Callback<Void> callback) {

        THREAD_POOL_EXECUTOR.execute(new Runnable() {
            Exception exception = null;

            @Override
            public void run() {
                try {
                    RestAdapter.this.restRunner.remove(id);
                } catch (Exception e) {
                    exception = e;
                }
                if (exception == null) {
                    callback.onSuccess(null);
                } else {
                    callback.onFailure(exception);
                }
            }
        });

    }

    @Override
    public void setAuthenticationModule(AuthenticationModule module) {
        this.restRunner.setAuthenticationModule(module);
    }

    /**
     * Sets the encoding of the Pipe. May not be null.
     *
     * @param encoding
     * @throws IllegalArgumentException if encoding is null
     */
    public void setEncoding(Charset encoding) {
        this.restRunner.setEncoding(encoding);
    }

    public String getDataRoot() {
        return this.restRunner.getDataRoot();
    }

    protected void setDataRoot(String dataRoot) {
        this.restRunner.setDataRoot(dataRoot);
    }

    public ParameterProvider getParameterProvider() {
        return parameterProvider;
    }

    protected void setParameterProvider(ParameterProvider parameterProvider) {
        this.parameterProvider = parameterProvider;
    }
}
