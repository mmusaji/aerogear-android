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

import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.aerogear.android.core.HeaderAndBody;
import org.aerogear.android.core.HttpException;
import org.aerogear.android.core.HttpProvider;

/**
 *
 *
 *
 * These are tuned for Aerogear, assume the body is String data, and that the
 * headers don't do anything funny.
 *
 *

 */
public final class HttpRestProvider implements HttpProvider {

    private static final String TAG = "AeroGear";
    private final URL url;
    private Map<String, String> defaultHeaders = new HashMap<String, String>();

    public HttpRestProvider(URL url) {
        this.url = url;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getUrl() {
        return url;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HeaderAndBody get() throws RuntimeException {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = prepareConnection();
            return getHeaderAndBody(urlConnection);

        } catch (IOException e) {
            Log.e(TAG, "Error on GET of " + url, e);
            throw new RuntimeException(e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HeaderAndBody post(String data) throws RuntimeException {
        HttpURLConnection urlConnection = null;

        try {
            urlConnection = prepareConnection();
            addBodyRequest(urlConnection, data); 
            return getHeaderAndBody(urlConnection);

        } catch (IOException e) {
            Log.e(TAG, "Error on POST of " + url, e);
            throw new RuntimeException(e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HeaderAndBody put(String id, String data) throws RuntimeException {
        HttpURLConnection urlConnection = null;
        
        try {
            urlConnection = prepareConnection(id);
            urlConnection.setRequestMethod("PUT");
            addBodyRequest(urlConnection, data);
            return getHeaderAndBody(urlConnection);
        } catch (IOException e) {
            Log.e(TAG, "Error on PUT of " + url, e);
            throw new RuntimeException(e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HeaderAndBody delete(String id) throws RuntimeException {
        
        
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = prepareConnection(id);
            return getHeaderAndBody(urlConnection);
        } catch (IOException e) {
            Log.e(TAG, "Error on DELETE of " + url, e);
            throw new RuntimeException(e);
        }finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private void addBodyRequest(HttpURLConnection urlConnection, String data) throws IOException {
        
        urlConnection.setDoOutput(true);
        urlConnection.setChunkedStreamingMode(0);

        OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
        out.write(data.getBytes());
    }


    private HttpURLConnection prepareConnection() throws IOException {
        return prepareConnection(null);
    }
    
    private HttpURLConnection prepareConnection(String id) throws IOException {
        URL resourceURL = this.url;
        if (id != null) {
            resourceURL = new URL(appendIdToURL(id));
        }
            
        HttpURLConnection urlConnection = (HttpURLConnection) resourceURL.openConnection();
        urlConnection.addRequestProperty("Accept", "application/json");
        urlConnection.addRequestProperty("Content-type", "application/json");

        for (Entry<String, String> entry : defaultHeaders.entrySet()) {
            urlConnection.addRequestProperty(entry.getKey(), entry.getValue());
        }

        return urlConnection;

    }

    private String appendIdToURL(String id) {
        StringBuilder newUrl = new StringBuilder(url.toString());
        if (!url.toString().endsWith("/")) {
            newUrl.append("/");
        }
        newUrl.append(id);
        return newUrl.toString();
    }

    @Override
    public void setDefaultHeader(String headerName, String headerValue) {
        defaultHeaders.put(headerName, headerValue);
    }
    private HeaderAndBody getHeaderAndBody(HttpURLConnection urlConnection) throws IOException {

        int statusCode = urlConnection.getResponseCode();
        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        byte[] data = readBytes(in);
        
        if (statusCode != 200) {
            throw new HttpException(data, statusCode);
        }

        Map<String, List<String>> headers = urlConnection.getHeaderFields();
        HeaderAndBody result = new HeaderAndBody(data, new HashMap<String, Object>(headers.size()));

        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            result.setHeader(header.getKey(), header.getValue().get(0));
        }

        return result;
    }

    private byte[] readBytes(InputStream inputStream) throws IOException {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }
}
