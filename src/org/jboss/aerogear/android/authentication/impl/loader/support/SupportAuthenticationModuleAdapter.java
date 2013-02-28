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
package org.jboss.aerogear.android.authentication.impl.loader.support;

import android.content.Context;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import com.google.common.base.Objects;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.authentication.AuthorizationFields;
import org.jboss.aerogear.android.authentication.impl.loader.ModernAuthenticationModuleAdapter;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.pipeline.support.AbstractFragmentActivityCallback;
import org.jboss.aerogear.android.pipeline.support.AbstractSupportFragmentCallback;

/**
 * This class manages the relationship between Android's Loader framework and
 * requests to Authentication. This class acts as a proxy for an
 * {@link AuthenticationModule} instance.
 *
 * This class uses the Android support versions of the Loader API. If you do not
 * need to support devices &lt; Android 3.0, see
 * {@link ModernAuthenticationModuleAdapter}
 */
public class SupportAuthenticationModuleAdapter implements AuthenticationModule, LoaderManager.LoaderCallbacks<HeaderAndBody> {

    private static final String TAG = SupportAuthenticationModuleAdapter.class.getSimpleName();
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
    private final FragmentActivity activity;
    private final Fragment fragment;
    private final Handler handler;
    private final String name;

    public SupportAuthenticationModuleAdapter(FragmentActivity activity, AuthenticationModule module, String name) {
        this.module = module;
        this.manager = activity.getSupportLoaderManager();
        this.applicationContext = activity.getApplicationContext();
        this.fragment = null;
        this.activity = activity;
        this.handler = new Handler(Looper.getMainLooper());
        this.name = name;
    }

    public SupportAuthenticationModuleAdapter(Fragment fragment, Context applicationContext, AuthenticationModule module, String name) {
        this.module = module;
        this.manager = fragment.getLoaderManager();
        this.applicationContext = applicationContext;
        this.fragment = fragment;
        this.activity = null;
        this.handler = new Handler(Looper.getMainLooper());
        this.name = name;
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
        bundle.putSerializable(METHOD, SupportAuthenticationModuleAdapter.Methods.ENROLL);
        manager.initLoader(id, bundle, this);
    }

    @Override
    public void login(String username, String password, Callback<HeaderAndBody> callback) {
        int id = Objects.hashCode(name, username, password, callback);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CALLBACK, callback);
        bundle.putSerializable(USERNAME, username);
        bundle.putSerializable(PASSWORD, password);
        bundle.putSerializable(METHOD, SupportAuthenticationModuleAdapter.Methods.LOGIN);
        manager.initLoader(id, bundle, this);
    }

    @Override
    public void logout(Callback<Void> callback) {
        int id = Objects.hashCode(name, callback);
        Bundle bundle = new Bundle();
        bundle.putSerializable(CALLBACK, callback);
        bundle.putSerializable(METHOD, SupportAuthenticationModuleAdapter.Methods.LOGOUT);
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
        SupportAuthenticationModuleAdapter.Methods method = (SupportAuthenticationModuleAdapter.Methods) bundle.get(METHOD);
        Callback callback = (Callback) bundle.get(CALLBACK);
        Loader loader = null;
        switch (method) {
        case LOGIN: {
            String username = bundle.getString(USERNAME);
            String password = bundle.getString(PASSWORD);
            loader = new SupportLoginLoader(applicationContext, callback, module, username, password);
        }
            break;
        case LOGOUT: {
            loader = new SupportLogoutLoader(applicationContext, callback, module);
        }
            break;
        case ENROLL: {
            Map<String, String> params = (Map<String, String>) bundle.getSerializable(PARAMS);
            loader = new SupportEnrollLoader(applicationContext, callback, module, params);
        }
            break;
        }
        return loader;
    }

    /**
     * This method will call the Callback for a enroll, login, or logout method
     * on the main thread of the application. If a callback is an instance of
     * {@link AbstractSupportFragmentCallback} or
     * {@link AbstractFragmentActivityCallback} then it will also configure the
     * reference to {@link Fragment} or {@link FragmentActivity} for the
     * callback.
     */
    @Override
    public void onLoadFinished(Loader<HeaderAndBody> loader, final HeaderAndBody data) {
        if (!(loader instanceof AbstractSupportAuthenticationLoader)) {
            Log.e(TAG, "Adapter is listening to loaders which it doesn't support");
            throw new IllegalStateException("Adapter is listening to loaders which it doesn't support");
        } else {
            final AbstractSupportAuthenticationLoader supportLoader = (AbstractSupportAuthenticationLoader) loader;

            if (supportLoader.hasException()) {
                final Exception exception = supportLoader.getException();
                Log.e(TAG, exception.getMessage(), exception);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (supportLoader.getCallback() instanceof AbstractSupportFragmentCallback) {
                            fragmentFailure(supportLoader.getCallback(), exception);
                        } else if (supportLoader.getCallback() instanceof AbstractFragmentActivityCallback) {
                            activityFailure(supportLoader.getCallback(), exception);
                        } else {
                            supportLoader.getCallback().onFailure(exception);
                        }
                    }
                });
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (supportLoader.getCallback() instanceof AbstractSupportFragmentCallback) {
                            fragmentSuccess(supportLoader.getCallback(), data);
                        } else if (supportLoader.getCallback() instanceof AbstractFragmentActivityCallback) {
                            activitySuccess(supportLoader.getCallback(), data);
                        } else {
                            supportLoader.getCallback().onSuccess(data);
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
        AbstractSupportFragmentCallback callback = (AbstractSupportFragmentCallback) typelessCallback;
        callback.setFragment(fragment);
        callback.onSuccess(data);
        callback.setFragment(null);
    }

    private void fragmentFailure(Callback<HeaderAndBody> typelessCallback, Exception exception) {
        AbstractSupportFragmentCallback callback = (AbstractSupportFragmentCallback) typelessCallback;
        callback.setFragment(fragment);
        callback.onFailure(exception);
        callback.setFragment(null);
    }

    private void activitySuccess(Callback<HeaderAndBody> typelessCallback, HeaderAndBody data) {
        AbstractFragmentActivityCallback callback = (AbstractFragmentActivityCallback) typelessCallback;
        callback.setFragmentActivity(activity);
        callback.onSuccess(data);
        callback.setFragmentActivity(null);
    }

    private void activityFailure(Callback<HeaderAndBody> typelessCallback, Exception exception) {
        AbstractFragmentActivityCallback callback = (AbstractFragmentActivityCallback) typelessCallback;
        callback.setFragmentActivity(activity);
        callback.onFailure(exception);
        callback.setFragmentActivity(null);
    }
}
