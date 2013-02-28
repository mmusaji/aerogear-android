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

import android.content.Context;
import android.util.Log;
import java.util.concurrent.CountDownLatch;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.http.HeaderAndBody;

public class ModernLogoutLoader extends AbstractModernAuthenticationLoader {

    private static final String TAG = ModernLogoutLoader.class.getSimpleName();
    
    public ModernLogoutLoader(Context context, Callback callback, AuthenticationModule module) {
        super(context, module, callback);
    }

    @Override
    public HeaderAndBody loadInBackground() {
        final CountDownLatch latch = new CountDownLatch(1);
        module.logout(new Callback<Void>() {

            @Override
            public void onSuccess(Void data) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                ModernLogoutLoader.super.setException(e);
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Log.e(TAG, ex.getMessage(), ex);
        }
        return null;
    }
    
    @Override
    protected void onStartLoading() {
        forceLoad();
    }
    
}
