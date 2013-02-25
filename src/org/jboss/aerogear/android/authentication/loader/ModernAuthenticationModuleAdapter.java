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
package org.jboss.aerogear.android.authentication.loader;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import java.net.URL;
import java.util.Map;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.authentication.AuthorizationFields;
import org.jboss.aerogear.android.http.HeaderAndBody;

public class ModernAuthenticationModuleAdapter implements AuthenticationModule, LoaderManager.LoaderCallbacks<HeaderAndBody>{

    @Override
    public URL getBaseURL() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLoginEndpoint() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLogoutEndpoint() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getEnrollEndpoint() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void enroll(Map<String, String> userData, Callback<HeaderAndBody> callback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void login(String username, String password, Callback<HeaderAndBody> callback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void logout(Callback<Void> callback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isLoggedIn() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AuthorizationFields getAuthorizationFields() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Loader<HeaderAndBody> onCreateLoader(int id, Bundle args) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onLoadFinished(Loader<HeaderAndBody> loader, HeaderAndBody data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onLoaderReset(Loader<HeaderAndBody> loader) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
