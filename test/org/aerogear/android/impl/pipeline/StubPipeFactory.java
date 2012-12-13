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

import java.net.MalformedURLException;
import java.net.URL;
import org.aerogear.android.Callback;
import org.aerogear.android.ReadFilter;
import org.aerogear.android.authentication.AuthenticationModule;
import org.aerogear.android.pipeline.Pipe;
import org.aerogear.android.pipeline.PipeFactory;
import org.aerogear.android.pipeline.PipeType;

public class StubPipeFactory implements PipeFactory {

    @Override
    public Pipe createPipe(Class klass, PipeConfig config) {
        return new Pipe() {
            @Override
            public PipeType getType() {
                return new PipeType() {
                    @Override
                    public String getName() {
                        return "Stub";
                    }
                };
            }

            @Override
            public URL getUrl() {
                try {
                    return new URL("http://myStubUrl/myStubProject");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void read(Callback callback) {
            }

            @Override
            public void readWithFilter(ReadFilter filer, Callback callback) {
            }

            @Override
            public void save(Object item, Callback callback) {
            }

            @Override
            public void remove(String id, Callback callback) {
            }

            @Override
            public void setAuthenticationModule(AuthenticationModule module) {
            }

        };
    }

}
