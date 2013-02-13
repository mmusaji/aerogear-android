/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.aerogear.android.impl.pipeline.worker;

import android.os.AsyncTask;
import android.util.Log;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.http.HttpProvider;
import org.jboss.aerogear.android.impl.pipeline.RestPipeWorker;

/**
 *
 * @author summers
 */
public class DeletePipeWorker<T> extends RestPipeWorker<T> {

    final String id;

    private static final String TAG = DeletePipeWorker.class.getSimpleName();
    
    public DeletePipeWorker(HttpProvider httpProvider, Serializable uuid, String toDelete) {
        super(httpProvider, uuid);
        this.id = toDelete;
    }

    @Override
    public void run() {
        
        
        new AsyncTask<Void, Void, Void>() {
            Exception exception = null;

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    HttpProvider httpProvider = getHttpProvider();
                    httpProvider.delete(id);
                } catch (Exception e) {
                    exception = e;
                }
                DeletePipeWorker.super.isFinished = true;
                return null;
            }

            @Override
            protected void onPostExecute(Void ignore) {
                super.onPostExecute(ignore);
                //lets not change callbacks after we finish.
                final Set<Callback> callbacks = Collections.unmodifiableSet(DeletePipeWorker.super.callbacks);
        
                for (Callback callback : callbacks) {
                    try {
                        if (exception == null) {
                            callback.onSuccess(null);
                        } else {
                            callback.onFailure(exception);
                        }
                    } catch (Throwable t) {//even if there is an exception, we should make it through everything
                        Log.e(TAG, t.getMessage(), t);
                    }
                }
            }
        }.execute();
    }
}
