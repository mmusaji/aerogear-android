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

import android.content.AsyncTaskLoader;
import android.content.Context;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.authentication.impl.loader.support.AbstractSupportAuthenticationLoader;
import org.jboss.aerogear.android.http.HeaderAndBody;

/**
 * This class provides a reference to the callback, authentication module, and 
 * possible exceptions for the authentication loaders, modules, and call backs 
 * which may use it.
 * 
 * This class and its subclasses use the Loaders from android.content and will not work on devices
 * &lt; Android 3.0.  For these devices see {@link AbstractSupportAuthenticationLoader}
 */
public abstract class AbstractAuthenticationLoader extends AsyncTaskLoader<HeaderAndBody> {

    protected final Callback callback;
    protected final AuthenticationModule module;
    private Exception exception;

    public AbstractAuthenticationLoader(Context context, AuthenticationModule module, Callback callback) {
        super(context);
        this.callback = callback;
        this.module = module;
    }

    public Callback getCallback() {
        return callback;
    }

    public AuthenticationModule getModule() {
        return module;
    }

    boolean hasException() {
        return exception != null;
    }

    public Exception getException() {
        return exception;
    }

    protected void setException(Exception exception) {
        this.exception = exception;
    }

}
