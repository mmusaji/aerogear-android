/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.aerogear.android.impl.pipeline;

import android.util.Pair;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.http.HttpProvider;
import org.jboss.aerogear.android.pipeline.PipeWorker;

/**
 *
 * @author summers
 */
public abstract class RestPipeWorker<T> implements PipeWorker<T>, Runnable {

    protected T data = null;
    protected boolean hasData = false;
    protected Set<Callback> callbacks = new HashSet<Callback>(10);
    protected final Serializable uuid;
    private final HttpProvider httpProvider;
    
    public RestPipeWorker(HttpProvider httpProvider, Serializable uuid) {
        this.uuid = uuid;
        this.httpProvider = httpProvider;
    }

    @Override
    public boolean hasData() {
        return hasData;
    }

    @Override
    public T getData() {
        return data;
    }

    @Override
    public void registerCallback(Callback<Pair<Serializable, T>> resultCallback) {
        callbacks.add(resultCallback);
    }

    @Override
    public void unregisterCallback(Callback<Pair<Serializable, T>> resultCallback) {
        callbacks.remove(resultCallback);
    }

    protected final HttpProvider getHttpProvider() {
        return httpProvider;
    }
    
}
