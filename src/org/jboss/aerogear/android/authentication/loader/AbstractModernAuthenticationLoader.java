/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.aerogear.android.authentication.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.http.HeaderAndBody;

/**
 *
 * @author summers
 */
public abstract class AbstractModernAuthenticationLoader extends AsyncTaskLoader<HeaderAndBody> {
    protected final Callback callback;
    protected final AuthenticationModule module;
    private Exception exception;

    public AbstractModernAuthenticationLoader(Context context, AuthenticationModule module, Callback callback) {
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
