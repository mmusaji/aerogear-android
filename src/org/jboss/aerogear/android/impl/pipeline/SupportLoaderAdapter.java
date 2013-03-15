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

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.google.common.base.Objects;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import java.net.URL;
import java.util.List;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.impl.pipeline.loader.support.AbstractSupportPipeLoader;
import org.jboss.aerogear.android.impl.pipeline.loader.support.SupportReadLoader;
import org.jboss.aerogear.android.impl.pipeline.loader.support.SupportRemoveLoader;
import org.jboss.aerogear.android.impl.pipeline.loader.support.SupportSaveLoader;
import org.jboss.aerogear.android.pipeline.LoaderPipe;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.PipeHandler;
import org.jboss.aerogear.android.pipeline.PipeType;
import org.jboss.aerogear.android.pipeline.support.AbstractFragmentActivityCallback;
import org.jboss.aerogear.android.pipeline.support.AbstractSupportFragmentCallback;

/**
 * This class wraps a Pipe in an asynchronous Loader.
 *
 * This classes uses Loaders from android.support. If you do not need to support
 * Android devices &lt; version 3.0, consider using {@link ModernLoaderAdapter}
 *
 */
public class SupportLoaderAdapter<T> implements LoaderPipe<T>, LoaderManager.LoaderCallbacks<T> {

    private static final String TAG = SupportLoaderAdapter.class.getSimpleName();
    private static final String CALLBACK = "org.jboss.aerogear.android.impl.pipeline.ModernClassLoader.CALLBACK";
    private static final String METHOD = "org.jboss.aerogear.android.impl.pipeline.ModernClassLoader.METHOD";
    private static final String FILTER = "org.jboss.aerogear.android.impl.pipeline.ModernClassLoader.FILTER";
    private static final String ITEM = "org.jboss.aerogear.android.impl.pipeline.ModernClassLoader.ITEM";
    private static final String REMOVE_ID = "org.jboss.aerogear.android.impl.pipeline.ModernClassLoader.REMOVIE_ID";
    private Multimap<String, Integer> idsForNamedPipes;
    private final Fragment fragment;
    private final FragmentActivity activity;
    private final Handler handler;

    private static enum Methods {

        READ, SAVE, REMOVE
    };

    private final Context applicationContext;
    private final Pipe<T> pipe;
    private final LoaderManager manager;
    private final Gson gson;
    /**
     * The name referred to in the idsForNamedPipes
     */
    private final String name;

    public SupportLoaderAdapter(FragmentActivity activity, Pipe<T> pipe, Gson gson, String name) {
        this.pipe = pipe;
        this.gson = gson;
        this.manager = activity.getSupportLoaderManager();
        this.applicationContext = activity.getApplicationContext();
        this.name = name;
        this.handler = new Handler(Looper.getMainLooper());
        this.activity = activity;
        this.fragment = null;
    }

    public SupportLoaderAdapter(Fragment fragment, Context applicationContext, Pipe<T> pipe, Gson gson, String name) {
        this.pipe = pipe;
        this.manager = fragment.getLoaderManager();
        this.gson = gson;
        this.applicationContext = applicationContext;
        this.name = name;
        this.handler = new Handler(Looper.getMainLooper());
        this.activity = null;
        this.fragment = fragment;
    }

    @Override
    public PipeType getType() {
        return pipe.getType();
    }

    @Override
    public URL getUrl() {
        return pipe.getUrl();
    }

    @Override
    public void read(Callback<List<T>> callback) {
        int id = Objects.hashCode(name, callback);

        Bundle bundle = new Bundle();
        bundle.putSerializable(CALLBACK, callback);
        bundle.putSerializable(FILTER, null);
        bundle.putSerializable(METHOD, Methods.READ);
        manager.initLoader(id, bundle, this);
    }

    @Override
    public void readWithFilter(ReadFilter filter, Callback<List<T>> callback) {
        int id = Objects.hashCode(name, filter, callback);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CALLBACK, callback);
        bundle.putSerializable(FILTER, filter);
        bundle.putSerializable(METHOD, Methods.READ);
        manager.initLoader(id, bundle, this);
    }

    @Override
    public void save(T item, Callback<T> callback) {
        int id = Objects.hashCode(name, item, callback);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CALLBACK, callback);
        bundle.putSerializable(ITEM, gson.toJson(item));//item may not be serializable, but it has to be gsonable
        bundle.putSerializable(METHOD, Methods.SAVE);
        manager.initLoader(id, bundle, this);
    }

    @Override
    public void remove(String toRemoveId, Callback<Void> callback) {
        int id = Objects.hashCode(name, toRemoveId, callback);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CALLBACK, callback);
        bundle.putSerializable(REMOVE_ID, toRemoveId);
        bundle.putSerializable(METHOD, Methods.REMOVE);
        manager.initLoader(id, bundle, this);
    }

    @Override
    public PipeHandler<T> getHandler() {
        return pipe.getHandler();
    }

    @Override
    public Loader<T> onCreateLoader(int id, Bundle bundle) {
        this.idsForNamedPipes.put(name, id);
        Methods method = (Methods) bundle.get(METHOD);
        Callback callback = (Callback) bundle.get(CALLBACK);
        Loader loader = null;
        switch (method) {
        case READ: {
            ReadFilter filter = (ReadFilter) bundle.get(FILTER);
            loader = new SupportReadLoader(applicationContext, callback, pipe.getHandler(), filter, this);
        }
            break;
        case REMOVE: {
            String toRemove = bundle.getString(REMOVE_ID, "-1");
            loader = new SupportRemoveLoader(applicationContext, callback, pipe.getHandler(), toRemove);
        }
            break;
        case SAVE: {
            String json = bundle.getString(ITEM);
            T item = gson.fromJson(json, pipe.getKlass());
            loader = new SupportSaveLoader(applicationContext, callback, pipe.getHandler(), item);
        }
            break;
        }
        return loader;
    }

    @Override
    public Gson getGson() {
        return gson;
    }

    @Override
    public Class<T> getKlass() {
        return pipe.getKlass();
    }

    @Override
    public void onLoadFinished(Loader<T> loader, final T data) {
        if (!(loader instanceof AbstractSupportPipeLoader)) {
            Log.e(TAG, "Adapter is listening to loaders which it doesn't support");
            throw new IllegalStateException("Adapter is listening to loaders which it doesn't support");
        } else {
            final AbstractSupportPipeLoader<T> supportLoader = (AbstractSupportPipeLoader<T>) loader;
            if (supportLoader.hasException()) {
                final Exception exception = supportLoader.getException();
                Log.e(TAG, exception.getMessage(), exception);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (supportLoader.callback instanceof AbstractSupportFragmentCallback) {
                            fragmentFailure(supportLoader.callback, exception);
                        } else if (supportLoader.callback instanceof AbstractFragmentActivityCallback) {
                            activityFailure(supportLoader.callback, exception);
                        } else {
                            supportLoader.callback.onFailure(exception);
                        }
                    }
                });

            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (supportLoader.callback instanceof AbstractSupportFragmentCallback) {
                            fragmentSuccess(supportLoader.callback, data);
                        } else if (supportLoader.callback instanceof AbstractFragmentActivityCallback) {
                            activitySuccess(supportLoader.callback, data);
                        } else {
                            supportLoader.callback.onSuccess(data);
                        }
                    }
                });

            }
        }
    }

    @Override
    public void onLoaderReset(Loader<T> loader) {
        //Gotta do something, though I don't know what
    }

    @Override
    public void reset() {
        for (Integer id : this.idsForNamedPipes.get(name)) {
            Loader loader = manager.getLoader(id);
            if (loader != null) {
                manager.destroyLoader(id);
            }
        }
        idsForNamedPipes.removeAll(name);
    }

    @Override
    public void setLoaderIds(Multimap<String, Integer> idsForNamedPipes) {
        this.idsForNamedPipes = idsForNamedPipes;
    }

    private void fragmentSuccess(Callback<T> typelessCallback, T data) {
        AbstractSupportFragmentCallback callback = (AbstractSupportFragmentCallback) typelessCallback;
        callback.setFragment(fragment);
        callback.onSuccess(data);
        callback.setFragment(null);
    }

    private void fragmentFailure(Callback<T> typelessCallback, Exception exception) {
        AbstractSupportFragmentCallback callback = (AbstractSupportFragmentCallback) typelessCallback;
        callback.setFragment(fragment);
        callback.onFailure(exception);
        callback.setFragment(null);
    }

    private void activitySuccess(Callback<T> typelessCallback, T data) {
        AbstractFragmentActivityCallback callback = (AbstractFragmentActivityCallback) typelessCallback;
        callback.setFragmentActivity(activity);
        callback.onSuccess(data);
        callback.setFragmentActivity(null);
    }

    private void activityFailure(Callback<T> typelessCallback, Exception exception) {
        AbstractFragmentActivityCallback callback = (AbstractFragmentActivityCallback) typelessCallback;
        callback.setFragmentActivity(activity);
        callback.onFailure(exception);
        callback.setFragmentActivity(null);
    }
}
