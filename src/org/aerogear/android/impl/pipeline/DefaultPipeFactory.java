/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.aerogear.android.impl.pipeline;

import android.util.Log;
import java.net.MalformedURLException;
import java.net.URL;
import org.aerogear.android.impl.core.HttpRestProvider;
import org.aerogear.android.pipeline.Pipe;
import org.aerogear.android.pipeline.PipeFactory;

public final class DefaultPipeFactory implements PipeFactory {


    @Override
    public <T> Pipe<T> createPipe(Class<T> klass, PipeConfig config) {
        Pipe<T> createdPipe;
        if (config.getType().equals(PipeTypes.REST)) {
            URL url = appendEndpoint(config.getBaseURL(), config.getEndpoint());
            HttpRestProvider httpProvider = new HttpRestProvider(url);

            if (config.getGsonBuilder() != null) {
                createdPipe = new RestAdapter<T>(klass, httpProvider, config.getGsonBuilder());
            } else {
                createdPipe = new RestAdapter<T>(klass, httpProvider);
            }

        } else {
            throw new IllegalArgumentException("Type is not supported yet");
        }

        if (config.getAuthModule() != null) {
            createdPipe.setAuthenticationModule(config.getAuthModule());
        }

        return createdPipe;
    }

    private static URL appendEndpoint(URL baseURL, String endpoint) {
        try {
            if (!baseURL.toString().endsWith("/")) {
                endpoint = "/" + endpoint;
            }
            return new URL(baseURL + endpoint );
        } catch (MalformedURLException e) {
            Log.e("AeroGear", e.getMessage());
            return null;
        }
    }

}
