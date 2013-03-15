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

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.common.base.Objects;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import java.net.URL;
import java.util.List;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.impl.pipeline.loader.AbstractModernPipeLoader;
import org.jboss.aerogear.android.impl.pipeline.loader.ModernReadLoader;
import org.jboss.aerogear.android.impl.pipeline.loader.ModernRemoveLoader;
import org.jboss.aerogear.android.impl.pipeline.loader.ModernSaveLoader;
import org.jboss.aerogear.android.pipeline.AbstractActivityCallback;
import org.jboss.aerogear.android.pipeline.AbstractFragmentCallback;
import org.jboss.aerogear.android.pipeline.LoaderPipe;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.PipeHandler;
import org.jboss.aerogear.android.pipeline.PipeType;

/**
 * This class wraps a Pipe in an asynchronous Loader.
 *
 * This classes uses Loaders from android.conent. It will not work on pre
 * Honeycomb devices. If you do need to support Android devices &lt; version
 * 3.0, consider using {@link SupportLoaderAdapter}
 *
 */
public class ModernLoaderAdapter<T> implements LoaderPipe<T>, LoaderManager.LoaderCallbacks<T> {

    private static final String TAG = ModernLoaderAdapter.class.getSimpleName();
    private static final String CALLBACK = "org.jboss.aerogear.android.impl.pipeline.ModernClassLoader.CALLBACK";
    private static final String METHOD = "org.jboss.aerogear.android.impl.pipeline.ModernClassLoader.METHOD";
    private static final String FILTER = "org.jboss.aerogear.android.impl.pipeline.ModernClassLoader.FILTER";
    private static final String ITEM = "org.jboss.aerogear.android.impl.pipeline.ModernClassLoader.ITEM";
    private static final String REMOVE_ID = "org.jboss.aerogear.android.impl.pipeline.ModernClassLoader.REMOVIE_ID";
    private final Handler handler;
    private Multimap<String, Integer> idsForNamedPipes;

    private static enum Methods {

        READ, SAVE, REMOVE
    };

    private final Context applicationContext;
    private Fragment fragment;
    private Activity activity;
    private final Pipe<T> pipe;
    private final LoaderManager manager;
    private final Gson gson;
    private final String name;

    public ModernLoaderAdapter(Activity activity, Pipe<T> pipe, Gson gson, String name) {
        this.pipe = pipe;
        this.gson = gson;
        this.manager = activity.getLoaderManager();
        this.applicationContext = activity.getApplicationContext();
        this.name = name;
        this.handler = new Handler(Looper.getMainLooper());
        this.activity = activity;
    }

    public ModernLoaderAdapter(Fragment fragment, Context applicationContext, Pipe<T> pipe, Gson gson, String name) {
        this.pipe = pipe;
        this.manager = fragment.getLoaderManager();
        this.gson = gson;
        this.applicationContext = applicationContext;
        this.name = name;
        this.handler = new Handler(Looper.getMainLooper());
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
    public Gson getGson() {
        return gson;
    }

    @Override
    public Class<T> getKlass() {
        return pipe.getKlass();
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
            loader = new ModernReadLoader(applicationContext, callback, pipe.getRunner(), filter, this);
        }
            break;
        case REMOVE: {
            String toRemove = bundle.getString(REMOVE_ID, "-1");
            loader = new ModernRemoveLoader(applicationContext, callback, pipe.getRunner(), toRemove);
        }
            break;
        case SAVE: {
            String json = bundle.getString(ITEM);
            T item = gson.fromJson(json, pipe.getKlass());
            loader = new ModernSaveLoader(applicationContext, callback, pipe.getRunner(), item);
        }
            break;
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<T> loader, final T data) {
        if (!(loader instanceof AbstractModernPipeLoader)) {
            Log.e(TAG, "Adapter is listening to loaders which it doesn't support");
            throw new IllegalStateException("Adapter is listening to loaders which it doesn't support");
        } else {
            final AbstractModernPipeLoader<T> modernLoader = (AbstractModernPipeLoader<T>) loader;
            if (modernLoader.hasException()) {
                final Exception exception = modernLoader.getException();
                Log.e(TAG, exception.getMessage(), exception);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (modernLoader.callback instanceof AbstractFragmentCallback) {
                            fragmentFailure(modernLoader.callback, exception);
                        } else if (modernLoader.callback instanceof AbstractActivityCallback) {
                            activityFailure(modernLoader.callback, exception);
                        } else {
                            modernLoader.callback.onFailure(exception);
                        }
                    }
                });

            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (modernLoader.callback instanceof AbstractFragmentCallback) {
                            fragmentSuccess(modernLoader.callback, data);
                        } else if (modernLoader.callback instanceof AbstractActivityCallback) {
                            activitySuccess(modernLoader.callback, data);
                        } else {
                            modernLoader.callback.onSuccess(data);
                        }
                    }
                });

            }
        }
    }

    @Override
    public void onLoaderReset(Loader<T> loader) {
        Log.e(TAG, loader.toString());

    }

    @Override
    public void reset() {
        for (Integer id : idsForNamedPipes.get(name)) {
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
        AbstractFragmentCallback callback = (AbstractFragmentCallback) typelessCallback;
        callback.setFragment(fragment);
        callback.onSuccess(data);
        callback.setFragment(null);
    }

    private void fragmentFailure(Callback<T> typelessCallback, Exception exception) {
        AbstractFragmentCallback callback = (AbstractFragmentCallback) typelessCallback;
        callback.setFragment(fragment);
        callback.onFailure(exception);
        callback.setFragment(null);
    }

    private void activitySuccess(Callback<T> typelessCallback, T data) {
        AbstractActivityCallback callback = (AbstractActivityCallback) typelessCallback;
        callback.setActivity(activity);
        callback.onSuccess(data);
        callback.setActivity(null);
    }

    private void activityFailure(Callback<T> typelessCallback, Exception exception) {
        AbstractActivityCallback callback = (AbstractActivityCallback) typelessCallback;
        callback.setActivity(activity);
        callback.onFailure(exception);
        callback.setActivity(null);
    }
}
