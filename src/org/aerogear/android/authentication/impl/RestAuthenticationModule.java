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
package org.aerogear.android.authentication.impl;

import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.aerogear.android.Callback;
import org.aerogear.android.Provider;
import org.aerogear.android.authentication.AuthenticationConfig;
import org.aerogear.android.authentication.AuthenticationModule;
import org.aerogear.android.core.HeaderAndBody;
import org.aerogear.android.core.HttpProvider;
import org.aerogear.android.impl.core.HttpRestProvider;
import org.json.JSONObject;

/**
 * A module for authenticating with restful AG services.
 */
public final class RestAuthenticationModule implements AuthenticationModule {


            private Provider<HttpProvider> httpProviderProvider = new Provider<HttpProvider>() {
        @Override
        public HttpProvider get(Object... in) {
            
            return new HttpRestProvider((URL)in[0]);
        }
    };

        
    private final URL baseURL;

    private final static Gson gson = new Gson();

    private final String loginEndpoint;
    private final URL loginURL;

    private final String logoutEndpoint;
    private final URL logoutURL;

    private final String enrollEndpoint;
    private final URL enrollURL;

    /**
     * This is the field which stores the AG security token.
     */
    private String authToken = "";

    /**
     * This is the name of the header to set for the token.
     */
    private final String tokenHeaderName;

    private boolean isLoggedIn = false;
    private static final String TAG = "RestAuthenticationModule";

    /**
     *
     * @param baseURL
     * @param config
     * @throws IllegalArgumentException if an endpoint can not be appended to
     * baseURL
     */
    public RestAuthenticationModule(URL baseURL, AuthenticationConfig config) {
        this.baseURL = baseURL;
        this.loginEndpoint = config.getLoginEndpoint();
        this.logoutEndpoint = config.getLogoutEndpoint();
        this.enrollEndpoint = config.getEnrollEndpoint();
        if (config instanceof RestAuthenticationConfig) {
            this.tokenHeaderName = ((RestAuthenticationConfig) config).getTokenHeaderName();
        } else {
            this.tokenHeaderName = "Auth-Token";
        }

        this.loginURL = appendToBaseURL(loginEndpoint);
        this.logoutURL = appendToBaseURL(logoutEndpoint);
        this.enrollURL = appendToBaseURL(enrollEndpoint);
    }

    @Override
    public URL getbaseURL() {
        return baseURL;
    }

    @Override
    public String getLoginEndpoint() {
        return loginEndpoint;
    }

    @Override
    public String getLogoutEndpoint() {
        return logoutEndpoint;
    }

    @Override
    public String getEnrollEndpoint() {
        return enrollEndpoint;
    }

    @Override
    public void enroll(final Map<String, String> userData, final Callback<HeaderAndBody> callback) {
        new AsyncTask<Void, Void, Void>() {
            HeaderAndBody result = null;
            Exception exception = null;

            @Override
            protected Void doInBackground(Void... params) {
                HttpProvider provider = httpProviderProvider.get( enrollURL );
                String enrollData = new JSONObject(userData).toString();
                try {
                    result = provider.post(enrollData);
                    authToken = result.getHeader(tokenHeaderName).toString();
                    isLoggedIn = true;

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

        }.execute((Void) null);
    }

    @Override
    public void login(final String username, final String password, final Callback<HeaderAndBody> callback) {
        new AsyncTask<Void, Void, Void>() {
            private Exception exception;
            private HeaderAndBody result;

            @Override
            protected Void doInBackground(Void... params) {
                HttpProvider provider = httpProviderProvider.get( loginURL);
                String loginData = buildLoginData(username, password);
                try {
                    result = provider.post(loginData);
                    authToken = result.getHeader(tokenHeaderName).toString();
                    isLoggedIn = true;
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

        }.execute((Void) null);

    }

    @Override
    public void logout(final Callback<Void> callback) {
        new AsyncTask<Void, Void, Void>() {
            private Exception exception;

            @Override
            protected Void doInBackground(Void... params) {
                HttpProvider provider = httpProviderProvider.get( logoutURL );
                try {
                    provider.post("");
                    authToken = "";
                    isLoggedIn = false;

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

        }.execute((Void) null);
    }

    @Override
    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    protected String getAuthToken() {
        return authToken;
    }

    private String buildLoginData(String username, String password) {
        JsonObject response = new JsonObject();
        response.addProperty("username", username);
        response.addProperty("password", password);
        return response.toString();
    }

    @Override
    public void onSecurityApplicationRequested(HttpProvider httpProvider) {
        httpProvider.setDefaultHeader(tokenHeaderName, authToken);
    }

    /**
     * 
     * @param endpoint
     * @return a new url baseUrl + endpoint
     * @throws IllegalArgumentException if baseUrl+endpoint is not a real url.
     */
    private URL appendToBaseURL(String endpoint) {
        try {
            return new URL(baseURL.toString() + endpoint);
        } catch (MalformedURLException ex) {
            String message = "Could not append " + endpoint + " to " + baseURL.toString();
            Log.e(TAG, message, ex);
            throw new IllegalArgumentException(message, ex);
        }

    }

}
