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
package org.jboss.aerogear.android.impl.pipeline.loader;

import android.content.Context;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.impl.pipeline.RestRunner;

public class ModernSaveLoader<T> extends AbstractModernPipeLoader<T> {

    private final RestRunner<T> runner;
    private final T data;
    private T result;

    public ModernSaveLoader(Context context, Callback<T> callback, RestRunner<T> runner, T data) {
        super(context, callback);
        this.runner = runner;
        this.data = data;
    }

    @Override
    public T loadInBackground() {
        try {
            return (result = runner.save(data));
        } catch (Exception e) {
            super.exception = e;
        }
        return null;
    }

    @Override
    protected void onStartLoading() {
        if (result != null) {
            deliverResult(result);
        } else {
            forceLoad();
        }
    }
}
