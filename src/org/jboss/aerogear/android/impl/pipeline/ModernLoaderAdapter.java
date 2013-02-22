/**
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

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import com.google.common.base.Objects;
import com.google.gson.Gson;
import java.net.URL;
import java.util.List;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.impl.pipeline.loader.AbstractModernPipeLoader;
import org.jboss.aerogear.android.impl.pipeline.loader.ModernReadLoader;
import org.jboss.aerogear.android.impl.pipeline.loader.ModernRemoveLoader;
import org.jboss.aerogear.android.impl.pipeline.loader.ModernSaveLoader;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.PipeType;

/**
 * This class wraps a Store in an asynchronous Loader.
 */
public class ModernLoaderAdapter<T> implements Pipe<T>, LoaderManager.LoaderCallbacks<T> {
    
    private static final String TAG = ModernLoaderAdapter.class.getSimpleName();
    private static final String CALLBACK = "org.jboss.aerogear.android.impl.pipeline.ModernClassLoader.CALLBACK";
    private static final String METHOD = "org.jboss.aerogear.android.impl.pipeline.ModernClassLoader.METHOD";
    private static final String FILTER = "org.jboss.aerogear.android.impl.pipeline.ModernClassLoader.FILTER";
    private static final String ITEM = "org.jboss.aerogear.android.impl.pipeline.ModernClassLoader.ITEM";
    private static final String REMOVE_ID = "org.jboss.aerogear.android.impl.pipeline.ModernClassLoader.REMOVIE_ID";

    private static enum Methods {

        READ, SAVE, REMOVE
    };
    private final Context applicationContext;
    private final RestAdapter<T> pipe;
    private final LoaderManager manager;
    private final Gson gson;

    public ModernLoaderAdapter(Activity activity, RestAdapter<T> pipe) {
        this.pipe = pipe;
        gson = pipe.getGSON();
        manager = activity.getLoaderManager();
        applicationContext = activity.getApplicationContext();
    }

    public ModernLoaderAdapter(Fragment fragment, Context applicationContext, RestAdapter<T> pipe) {
        this.pipe = pipe;
        manager = fragment.getLoaderManager();
        gson = pipe.getGSON();
        this.applicationContext = applicationContext;
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
        int id = Objects.hashCode(callback);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CALLBACK, callback);
        bundle.putSerializable(FILTER, null);
        bundle.putSerializable(METHOD, Methods.READ);
        manager.initLoader(id, bundle, this);
    }

    @Override
    public void readWithFilter(ReadFilter filter, Callback<List<T>> callback) {
        int id = Objects.hashCode(filter, callback);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CALLBACK, callback);
        bundle.putSerializable(FILTER, filter);
        bundle.putSerializable(METHOD, Methods.READ);
        manager.initLoader(id, bundle, this);
    }

    @Override
    public void save(T item, Callback<T> callback) {
        int id = Objects.hashCode(item, callback);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CALLBACK, callback);
        bundle.putSerializable(ITEM, gson.toJson(item));//item may not be serializable, but it has to be gsonable
        bundle.putSerializable(METHOD, Methods.SAVE);
        manager.initLoader(id, bundle, this);
    }

    @Override
    public void remove(String toRemoveId, Callback<Void> callback) {
        int id = Objects.hashCode(toRemoveId, callback);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CALLBACK, callback);
        bundle.putSerializable(REMOVE_ID, toRemoveId);
        bundle.putSerializable(METHOD, Methods.REMOVE);
        manager.initLoader(id, bundle, this);
    }

    @Override
    public void setAuthenticationModule(AuthenticationModule module) {
        pipe.setAuthenticationModule(module);
    }

    @Override
    public Loader<T> onCreateLoader(int id, Bundle bundle) {
        Methods method = (Methods) bundle.get(METHOD);
        Callback callback = (Callback) bundle.get(CALLBACK);
        Loader l = null;
        switch (method) {
            case READ: {
                ReadFilter filter = (ReadFilter) bundle.get(FILTER);
                l = new ModernReadLoader(applicationContext, callback, pipe.getRunner(), filter, this);
            }
            break;
            case REMOVE: {
                String toRemove = bundle.getString(REMOVE_ID, "-1");
                l = new ModernRemoveLoader(applicationContext, callback, pipe.getRunner(), toRemove);
            }
            break;
            case SAVE: {
                String json = bundle.getString(ITEM);
                T item = gson.fromJson(json, pipe.getKlass());
                l= new ModernSaveLoader(applicationContext, callback, pipe.getRunner(), item);
            }
            break;
        }
        return l;
    }

    @Override
    public void onLoadFinished(Loader<T> loader, T data) {
        if (!(loader instanceof AbstractModernPipeLoader)) {
            Log.e(TAG, "Adapter is listening to loaders which it doesn't support");
            throw new IllegalStateException("Adapter is listening to loaders which it doesn't support");
        } else {
            AbstractModernPipeLoader modernLoader = (AbstractModernPipeLoader) loader;
            if (modernLoader.hasException()) {
            	Exception exception = modernLoader.getException();
            	Log.e(TAG, exception.getMessage(), exception);
                modernLoader.callback.onFailure(exception);
            } else {
                modernLoader.callback.onSuccess(data);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<T> loader) {
        //Gotta do something, though I don't know what
    }
}
