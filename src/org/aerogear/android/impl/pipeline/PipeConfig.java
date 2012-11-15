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

import com.google.gson.GsonBuilder;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import org.aerogear.android.authentication.AuthenticationModule;
import org.aerogear.android.pipeline.PipeType;

public final class PipeConfig {

public final class PipeConfig {


    private URL baseURL;
    private String name;
    private String endpoint;
    private PipeType type = PipeTypes.REST;
    private GsonBuilder gsonBuilder;
    private AuthenticationModule authModule;
    private Charset encoding = Charset.forName("UTF-8");

    public PipeConfig(URL baseURL, Class klass) {
        this.baseURL = baseURL;
        this.name = klass.getSimpleName().toLowerCase();
        this.endpoint = name;
        this.type = PipeTypes.REST;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URL getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(URL baseURL) {
        this.baseURL = baseURL;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public PipeType getType() {
        return type;
    }

    public void setType(PipeType type) {
        this.type = type;
    }

    public GsonBuilder getGsonBuilder() {
        return gsonBuilder;
    }

    public void setGsonBuilder(GsonBuilder gsonBuilder) {
        this.gsonBuilder = gsonBuilder;
    }

    public AuthenticationModule getAuthModule() {
        return authModule;
    }

    public void setAuthModule(AuthenticationModule authModule) {
        this.authModule = authModule;
    }

    /**
     * @return the current encoding, will not be null.
     */
    public Charset getEncoding() {
        return encoding;
    }

    /**
     * @param encoding a not null encoding
     * @throws IllegalArgumentException if encoding is null
     */
    public void setEncoding(Charset encoding) {
        if (encoding == null) {
            throw new IllegalArgumentException("Encoding may not be null");
        }
        this.encoding = encoding;
    }

    /**
     * @param charsetName a string for the encoding to be used
     * @throws UnsupportedCharsetException if charSet is not supported
     */
    public void setEncoding(String charsetName) {
        this.encoding = Charset.forName(charsetName);
    }

}
