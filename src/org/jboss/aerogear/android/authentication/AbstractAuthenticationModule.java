/*
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
package org.jboss.aerogear.android.authentication;

import android.os.AsyncTask;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.http.HeaderAndBody;

import java.util.Map;

/**
 * This class stubs out the enroll, login, and logout methods. If you call these
 * methods without overriding them they will throw an IllegalStateException in
 * the callback. This will be passed to onFailure as normal.
 */
public abstract class AbstractAuthenticationModule implements
        AuthenticationModule {

    public void enroll(Map<String, String> userData,
            final Callback<HeaderAndBody> callback) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                callback.onFailure(new IllegalStateException("Not implemented"));
            }
        }.execute((Void) null);

    }

    public void login(final String username, final String password,
            final Callback<HeaderAndBody> callback) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                callback.onFailure(new IllegalStateException("Not implemented"));
            }
        }.execute((Void) null);

    }

    public void logout(final Callback<Void> callback) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                callback.onFailure(new IllegalStateException("Not implemented"));
            }
        }.execute((Void) null);
    }

}
