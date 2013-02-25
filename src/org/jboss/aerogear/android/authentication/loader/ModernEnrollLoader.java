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

import android.content.AsyncTaskLoader;
import android.content.Context;
import java.util.Map;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.http.HeaderAndBody;

public class ModernEnrollLoader extends AsyncTaskLoader<HeaderAndBody> {
    private final Map<String, String> params;
    private final Callback callback;
    private final AuthenticationModule module;

    ModernEnrollLoader(Context context, Callback callback, AuthenticationModule module, Map<String, String> params) {
        super(context);
        this.callback = callback;
        this.params = params;
        this.module = module;
    }

    
    
    @Override
    public HeaderAndBody loadInBackground() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
