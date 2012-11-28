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
package org.aerogear.android.authentication;

import java.net.URL;
import java.util.Map;
import org.aerogear.android.Callback;
import org.aerogear.android.core.HeaderAndBody;
import org.aerogear.android.core.HttpProvider;
import org.aerogear.android.pipeline.Pipe;

/**
 * A module which can authenticate a user.  It also provides the
 * necessary tools to log a user in, out, and modify requests from a
 * {@link Pipe} so they are seen as authenticated.
 * <p/>
 * If you have fields which need to be passed as tokens to the server,
 * annotate them with {@link AuthValue}
 */
public interface AuthenticationModule {

    public URL getBaseURL();

    public String getLoginEndpoint();

    public String getLogoutEndpoint();

    public String getEnrollEndpoint();

    /**
     * Will try to register a user with a service using userData.
     * <p/>
     * It will call the callbacks onSuccess with a parameter of a Map of the
     * values returned by the enroll service or onFailure if there is an error
     *
     * @param userData
     * @param callback
     */
    public void enroll(Map<String, String> userData, Callback<HeaderAndBody> callback);

    /**
     * Will try to log in a user using username and password.
     * <p/>
     * It will call the callbacks onSuccess with a parameter of a Map of the
     * values returned by the enroll service or onFailure if there is an error
     *
     * @param username
     * @param password
     * @param callback
     */
    public void login(String username, String password, Callback<HeaderAndBody> callback);

    /**
     * Performs a logout of the current user.
     * <p/>
     * It will call callback.onSuccess with no value on success and
     * callback.onFailure if there is an error.
     *
     * @param callback
     */
    public void logout(Callback<Void> callback);

    /**
     * @return true if the module has been logged in successfully
     */
    public boolean isLoggedIn();

    /**
     * This method is called be {@link Pipe} implementations when they need
     * security applied to their {@link HttpProvider}.  This is usually done
     * directly before a request is made.
     *
     * @param httpProvider
     */
    public void onSecurityApplicationRequested(HttpProvider httpProvider);
}
