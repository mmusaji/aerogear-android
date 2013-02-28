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
package org.jboss.aerogear.android.authentication.impl.loader.support;


import android.content.Context;
import android.util.Log;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.http.HeaderAndBody;

public class SupportEnrollLoader extends AbstractSupportAuthenticationLoader { 
    
    private static final String TAG = SupportEnrollLoader.class.getSimpleName();
    
    private HeaderAndBody result = null;
    private final Map<String, String> params;
    
    
    public SupportEnrollLoader(Context context, Callback callback, AuthenticationModule module, Map<String, String> params) {
        super(context, module, callback);
        this.params = params;
    }

    @Override
    public HeaderAndBody loadInBackground() {
        final CountDownLatch latch = new CountDownLatch(1);
        module.enroll(params, new Callback<HeaderAndBody>() {

            @Override
            public void onSuccess(HeaderAndBody data) {
                result = data;
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                SupportEnrollLoader.super.setException(e);
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Log.e(TAG, ex.getMessage(), ex);
        }
        return result;
    }
    
    @Override
    protected void onStartLoading() {
        if (result == null) {
            forceLoad();
        } else {
            deliverResult(result);
        }
    }
    
}
