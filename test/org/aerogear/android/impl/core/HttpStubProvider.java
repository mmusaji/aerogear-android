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

package org.aerogear.android.impl.core;

import java.net.URL;
import org.aerogear.android.core.HeaderAndBody;
import org.aerogear.android.core.HttpProvider;

public class HttpStubProvider implements HttpProvider {

    private final URL url;

    public HttpStubProvider(URL url) {
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }

    public HeaderAndBody get() {
        return null;
    }

    public HeaderAndBody post(String data) {
        return null;
    }

    public HeaderAndBody put(String id, String data) {
        return null;
    }

    public HeaderAndBody delete(String id) {
        return null;
    }

    @Override
    public void setDefaultHeader(String headerName, String headerValue) {
    }

}
