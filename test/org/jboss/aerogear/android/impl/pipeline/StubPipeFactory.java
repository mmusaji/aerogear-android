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

package org.jboss.aerogear.android.impl.pipeline;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.PipeFactory;
import org.jboss.aerogear.android.pipeline.PipeType;
import org.jboss.aerogear.android.pipeline.PipeWorker;

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
            public Serializable read(Callback callback) {
                return null;
            }

            @Override
            public Serializable readWithFilter(ReadFilter filer, Callback callback) {
                return null;
            }

            @Override
            public Serializable save(Object item, Callback callback) {
                return null;
            }

            @Override
            public Serializable remove(String id, Callback callback) {
                return null;
            }

            @Override
            public void setAuthenticationModule(AuthenticationModule module) {
            }

            @Override
            public PipeWorker getWorker(Serializable id) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        };
    }

}
