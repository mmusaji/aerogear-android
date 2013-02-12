/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.aerogear.android.impl.pipeline.worker;

import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Set;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.http.HttpProvider;
import org.jboss.aerogear.android.impl.pipeline.RestPipeWorker;

/**
 *
 * @author summers
 */
public class SavePipeWorker<T>  extends RestPipeWorker<T> {

    private final String id;
    private final Gson gson;
    private static final String TAG = DeletePipeWorker.class.getSimpleName();
    private final Charset encoding;
    private final Class<T> klass;
    
    public SavePipeWorker(HttpProvider httpProvider, Serializable uuid, String toSave, Gson gson, Charset encoding, Class klass) {
        super(httpProvider, uuid);
        this.id = toSave;
        this.gson = gson;
        this.encoding = encoding;
        this.klass = klass;
    }

    @Override
    public void run() {
        new AsyncTask<Void, Void, Void>() {
            T result = null;
            Exception exception = null;

            @Override
            protected Void doInBackground(Void... voids) {
                try {

                    String body = gson.toJson(data);
                    final HttpProvider httpProvider = getHttpProvider();

                    HeaderAndBody requestResult;
                    if (id == null || id.length() == 0) {
                        requestResult = httpProvider.post(body);
                    } else {
                        requestResult = httpProvider.put(id, body);
                    }

                    this.result = gson.fromJson(new String(requestResult.getBody(), encoding), klass);

                } catch (Exception e) {
                    exception = e;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void ignore) {
                super.onPostExecute(ignore);
                //lets not change callbacks after we finish.
                final Set<Callback> callbacks = Collections.unmodifiableSet(SavePipeWorker.super.callbacks);
        
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
