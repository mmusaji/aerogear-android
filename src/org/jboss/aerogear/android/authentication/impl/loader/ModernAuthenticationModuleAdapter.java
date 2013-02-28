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
package org.jboss.aerogear.android.authentication.impl.loader;

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
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.authentication.AuthorizationFields;
import org.jboss.aerogear.android.authentication.impl.loader.support.SupportAuthenticationModuleAdapter;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.pipeline.AbstractActivityCallback;
import org.jboss.aerogear.android.pipeline.AbstractFragmentCallback;

/**
 * This class manages the relationship between Android's Loader framework and
 * requests to Authentication. This class acts as a proxy for an
 * {@link AuthenticationModule} instance.
 *
 * This class instantiates the Loaders from android.content and will not work on
 * devices &lt; Android 3.0. For these devices see
 * {@link SupportAuthenticationModuleAdapter }
 */
public class ModernAuthenticationModuleAdapter implements AuthenticationModule, LoaderManager.LoaderCallbacks<HeaderAndBody> {

    private static final String TAG = ModernAuthenticationModuleAdapter.class.getSimpleName();
    private static final String CALLBACK = "org.jboss.aerogear.android.authentication.loader.ModernAuthenticationModuleAdapter.CALLBACK";
    private static final String METHOD = "org.jboss.aerogear.android.authentication.loader.ModernAuthenticationModuleAdapter.METHOD";
    private static final String USERNAME = "org.jboss.aerogear.android.authentication.loader.ModernAuthenticationModuleAdapter.USERNAME";
    private static final String PASSWORD = "org.jboss.aerogear.android.authentication.loader.ModernAuthenticationModuleAdapter.PASSWORD";
    private static final String PARAMS = "org.jboss.aerogear.android.authentication.loader.ModernAuthenticationModuleAdapter.PARAMS";

    private static enum Methods {

        LOGIN, LOGOUT, ENROLL
    };

    private final Context applicationContext;
    private final AuthenticationModule module;
    private final LoaderManager manager;
    private final Activity activity;
    private final Fragment fragment;
    private final String name;
    private final Handler handler;

    public ModernAuthenticationModuleAdapter(Activity activity, AuthenticationModule module, String name) {
        this.module = module;
        this.manager = activity.getLoaderManager();
        this.applicationContext = activity.getApplicationContext();
        this.activity = activity;
        this.fragment = null;
        this.name = name;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public ModernAuthenticationModuleAdapter(Fragment fragment, Context applicationContext, AuthenticationModule module, String name) {
        this.module = module;
        this.manager = fragment.getLoaderManager();
        this.applicationContext = applicationContext;
        this.activity = null;
        this.fragment = fragment;
        this.name = name;
        this.handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public URL getBaseURL() {
        return module.getBaseURL();
    }

    @Override
    public String getLoginEndpoint() {
        return module.getLoginEndpoint();
    }

    @Override
    public String getLogoutEndpoint() {
        return module.getLogoutEndpoint();
    }

    @Override
    public String getEnrollEndpoint() {
        return module.getEnrollEndpoint();
    }

    @Override
    public void enroll(Map<String, String> userData, Callback<HeaderAndBody> callback) {
        int id = Objects.hashCode(name, userData, callback);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CALLBACK, callback);
        bundle.putSerializable(PARAMS, new HashMap(userData));
        bundle.putSerializable(METHOD, ModernAuthenticationModuleAdapter.Methods.ENROLL);
        manager.initLoader(id, bundle, this);
    }

    @Override
    public void login(String username, String password, Callback<HeaderAndBody> callback) {
        int id = Objects.hashCode(name, username, password, callback);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CALLBACK, callback);
        bundle.putSerializable(USERNAME, username);
        bundle.putSerializable(PASSWORD, password);
        bundle.putSerializable(METHOD, ModernAuthenticationModuleAdapter.Methods.LOGIN);
        manager.initLoader(id, bundle, this);
    }

    @Override
    public void logout(Callback<Void> callback) {
        int id = Objects.hashCode(name, callback);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CALLBACK, callback);
        bundle.putSerializable(METHOD, ModernAuthenticationModuleAdapter.Methods.LOGOUT);
        manager.initLoader(id, bundle, this);
    }

    @Override
    public boolean isLoggedIn() {
        return module.isLoggedIn();
    }

    @Override
    public AuthorizationFields getAuthorizationFields() {
        return module.getAuthorizationFields();
    }

    @Override
    public Loader<HeaderAndBody> onCreateLoader(int id, Bundle bundle) {
        ModernAuthenticationModuleAdapter.Methods method = (ModernAuthenticationModuleAdapter.Methods) bundle.get(METHOD);
        Callback callback = (Callback) bundle.get(CALLBACK);
        Loader loader = null;
        switch (method) {
        case LOGIN: {
            String username = bundle.getString(USERNAME);
            String password = bundle.getString(PASSWORD);
            loader = new ModernLoginLoader(applicationContext, callback, module, username, password);
        }
            break;
        case LOGOUT: {
            loader = new ModernLogoutLoader(applicationContext, callback, module);
        }
            break;
        case ENROLL: {
            Map<String, String> params = (Map<String, String>) bundle.getSerializable(PARAMS);
            loader = new ModernEnrollLoader(applicationContext, callback, module, params);
        }
            break;
        }
        return loader;
    }

    /**
     * This method will call the Callback for a enroll, login, or logout method
     * on the main thread of the application. If a callback is an instance of
     * {@link AbstractFragmentCallback} or {@link AbstractActivityCallback} then
     * it will also configure the reference to {@link Fragment} or
     * {@link FragmentActivity} for the callback.
     */
    @Override
    public void onLoadFinished(Loader<HeaderAndBody> loader, final HeaderAndBody data) {
        if (!(loader instanceof AbstractModernAuthenticationLoader)) {
            Log.e(TAG, "Adapter is listening to loaders which it doesn't support");
            throw new IllegalStateException("Adapter is listening to loaders which it doesn't support");
        } else {
            final AbstractModernAuthenticationLoader modernLoader = (AbstractModernAuthenticationLoader) loader;
            if (modernLoader.hasException()) {
                final Exception exception = modernLoader.getException();
                Log.e(TAG, exception.getMessage(), exception);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (modernLoader.getCallback() instanceof AbstractFragmentCallback) {
                            fragmentFailure(modernLoader.getCallback(), exception);
                        } else if (modernLoader.getCallback() instanceof AbstractActivityCallback) {
                            activityFailure(modernLoader.getCallback(), exception);
                        } else {
                            modernLoader.getCallback().onFailure(exception);
                        }
                    }
                });
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (modernLoader.getCallback() instanceof AbstractFragmentCallback) {
                            fragmentSuccess(modernLoader.getCallback(), data);
                        } else if (modernLoader.getCallback() instanceof AbstractActivityCallback) {
                            activitySuccess(modernLoader.getCallback(), data);
                        } else {
                            modernLoader.getCallback().onSuccess(data);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<HeaderAndBody> loader) {
        //Do nothing, should call logout on module manually.
    }

    private void fragmentSuccess(Callback<HeaderAndBody> typelessCallback, HeaderAndBody data) {
        AbstractFragmentCallback callback = (AbstractFragmentCallback) typelessCallback;
        callback.setFragment(fragment);
        callback.onSuccess(data);
        callback.setFragment(null);
    }

    private void fragmentFailure(Callback<HeaderAndBody> typelessCallback, Exception exception) {
        AbstractFragmentCallback callback = (AbstractFragmentCallback) typelessCallback;
        callback.setFragment(fragment);
        callback.onFailure(exception);
        callback.setFragment(null);
    }

    private void activitySuccess(Callback<HeaderAndBody> typelessCallback, HeaderAndBody data) {
        AbstractActivityCallback callback = (AbstractActivityCallback) typelessCallback;
        callback.setActivity(activity);
        callback.onSuccess(data);
        callback.setActivity(null);
    }

    private void activityFailure(Callback<HeaderAndBody> typelessCallback, Exception exception) {
        AbstractActivityCallback callback = (AbstractActivityCallback) typelessCallback;
        callback.setActivity(activity);
        callback.onFailure(exception);
        callback.setActivity(null);
    }
}
