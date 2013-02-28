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
package org.jboss.aerogear.android.authentication.impl.loader;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import com.google.common.base.Objects;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.authentication.AuthorizationFields;
import org.jboss.aerogear.android.http.HeaderAndBody;

public class ModernAuthenticationModuleAdapter implements AuthenticationModule, LoaderManager.LoaderCallbacks<HeaderAndBody>{

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
    
    
    public ModernAuthenticationModuleAdapter(Activity activity, AuthenticationModule module, String name) {
        this.module = module;
        this.manager = activity.getLoaderManager();
        this.applicationContext = activity.getApplicationContext();
        this.activity = activity;
        this.fragment = null;
        this.name = name;
    }

    public ModernAuthenticationModuleAdapter(Fragment fragment, Context applicationContext, AuthenticationModule module, String name) {
        this.module = module;
        this.manager = fragment.getLoaderManager();
        this.applicationContext = applicationContext;
        this.activity = null;
        this.fragment = fragment;
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
        Loader l = null;
        switch (method) {
            case LOGIN: {
                String username = bundle.getString(USERNAME);
                String password = bundle.getString(PASSWORD);
                l = new ModernLoginLoader(applicationContext, callback, module, username, password);
            }
            break;
            case LOGOUT: {
                l = new ModernLogoutLoader(applicationContext, callback, module);
            }
            break;
            case ENROLL: {
                Map<String, String> params = (Map<String, String>) bundle.getSerializable(PARAMS);
                l= new ModernEnrollLoader(applicationContext, callback, module, params);
            }
            break;
        }
        return l;
    }

    @Override
    public void onLoadFinished(Loader<HeaderAndBody> loader, HeaderAndBody data) {
        if (!(loader instanceof AbstractModernAuthenticationLoader)) {
            Log.e(TAG, "Adapter is listening to loaders which it doesn't support");
            throw new IllegalStateException("Adapter is listening to loaders which it doesn't support");
        } else {
            AbstractModernAuthenticationLoader modernLoader = (AbstractModernAuthenticationLoader) loader;
            if (modernLoader.hasException()) {
            	Exception exception = modernLoader.getException();
            	Log.e(TAG, exception.getMessage(), exception);
                modernLoader.getCallback().onFailure(exception);
            } else {
                modernLoader.getCallback().onSuccess(data);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<HeaderAndBody> loader) {
        //Do nothing, should call logout on module manually.
    }
    
}
