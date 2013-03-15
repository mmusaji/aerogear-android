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
import android.util.Log;
import com.google.common.base.Objects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.URL;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.PipeType;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jboss.aerogear.android.pipeline.PipeHandler;
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
    private final PipeHandler<T> restRunner;
    private final Gson gson;

    public RestAdapter(Class<T> klass, URL baseURL) {
        this.restRunner = new RestRunner(klass, baseURL);
        this.klass = klass;
        this.gson = new Gson();
        this.baseURL = baseURL;
    }

    public RestAdapter(Class<T> klass, URL baseURL, PipeConfig config) {
        this.klass = klass;
        this.baseURL = baseURL;
        this.gson = Objects.firstNonNull(config.getGsonBuilder(), new GsonBuilder()).create();

        if (config.getHandler() != null) {
            this.restRunner = config.getHandler();
        } else {
            this.restRunner = new RestRunner(klass, baseURL, config);
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
                    this.result = restRunner.onReadWithFilter(innerFilter, RestAdapter.this);
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
                    result = restRunner.onSave(data);
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
                    RestAdapter.this.restRunner.onRemove(id);
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
    public PipeHandler getHandler() {
        return restRunner;
    }

    @Override
    public Class<T> getKlass() {
        return klass;
    }

    @Override
    public Gson getGson() {
        return gson;
    }
}
