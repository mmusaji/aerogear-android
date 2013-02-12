/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and
 * individual contributors by the
 *
 * @authors tag. See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.jboss.aerogear.android.impl.pipeline;

import android.util.Log;
import android.util.Pair;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.client.utils.URIUtils;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.Provider;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.authentication.AuthorizationFields;
import org.jboss.aerogear.android.datamanager.IdGenerator;
import org.jboss.aerogear.android.http.HttpProvider;
import org.jboss.aerogear.android.impl.core.HttpProviderFactory;
import org.jboss.aerogear.android.impl.datamanager.DefaultIdGenerator;
import org.jboss.aerogear.android.impl.pipeline.paging.DefaultParameterProvider;
import org.jboss.aerogear.android.impl.pipeline.paging.URIBodyPageParser;
import org.jboss.aerogear.android.impl.pipeline.paging.URIPageHeaderParser;
import org.jboss.aerogear.android.impl.pipeline.worker.DeletePipeWorker;
import org.jboss.aerogear.android.impl.pipeline.worker.ReadPipeWorker;
import org.jboss.aerogear.android.impl.pipeline.worker.SavePipeWorker;
import org.jboss.aerogear.android.impl.reflection.Property;
import org.jboss.aerogear.android.impl.reflection.Scan;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.PipeType;
import org.jboss.aerogear.android.pipeline.PipeWorker;
import org.jboss.aerogear.android.pipeline.paging.PageConfig;
import org.jboss.aerogear.android.pipeline.paging.ParameterProvider;

/**
 * Rest implementation of {@link Pipe}.
 */
public final class RestAdapter<T> implements Pipe<T> {

    private final PageConfig pageConfig;
    private static final String TAG = RestAdapter.class.getSimpleName();
    private static final IdGenerator ID_GENERATOR = new DefaultIdGenerator();
    private static final ExecutorService POOL = Executors.newFixedThreadPool(5);
    private final HashMap<Serializable, PipeWorker> runningWorkers = new HashMap<Serializable, PipeWorker>(10);
    private final Cache<Serializable, PipeWorker<T>> finishedWorkers = CacheBuilder.newBuilder().maximumSize(10).build();
    private final Gson gson;
    private String dataRoot = "";
    private ParameterProvider parameterProvider = new DefaultParameterProvider();
    /**
     * A class of the Generic type this pipe wraps. This is used by GSON for
     * deserializing.
     */
    private final Class<T> klass;
    /**
     * A class of the Generic collection type this pipe wraps. This is used by
     * JSON for deserializing collections.
     */
    
    private final URL baseURL;
    private final Provider<HttpProvider> httpProviderFactory = new HttpProviderFactory();
    private AuthenticationModule authModule;
    private Charset encoding = Charset.forName("UTF-8");

    public RestAdapter(Class<T> klass, URL baseURL) {
        this.klass = klass;
        this.baseURL = baseURL;
        this.gson = new Gson();
        this.pageConfig = null;
    }

    public RestAdapter(Class<T> klass, URL baseURL,
            GsonBuilder gsonBuilder) {
        this.klass = klass;
        this.baseURL = baseURL;
        this.gson = gsonBuilder.create();
        this.pageConfig = null;
    }

    public RestAdapter(Class<T> klass, URL baseURL, PageConfig pageconfig) {
        this.klass = klass;
        this.baseURL = baseURL;
        this.gson = new Gson();
        this.pageConfig = pageconfig;
    }

    public RestAdapter(Class<T> klass, URL baseURL,
            GsonBuilder gsonBuilder, PageConfig pageconfig) {
        this.klass = klass;
        this.baseURL = baseURL;
        this.gson = gsonBuilder.create();
        this.pageConfig = pageconfig;
        if (pageconfig != null) {
            if (pageconfig.getPageHeaderParser() == null) {
                if (PageConfig.MetadataLocations.BODY.equals(pageconfig.getMetadataLocation())) {
                    pageconfig.setPageHeaderParser(new URIBodyPageParser(baseURL));
                } else if (PageConfig.MetadataLocations.HEADERS.equals(pageconfig.getMetadataLocation())) {
                    pageconfig.setPageHeaderParser(new URIPageHeaderParser(baseURL));
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PipeType getType() {
        return PipeTypes.REST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getUrl() {
        return baseURL;
    }

    @Override
    public Serializable readWithFilter(ReadFilter filter, final Callback<Pair<Serializable, List<T>>> resultCallback) {
        
        Serializable uuid = ID_GENERATOR.generate();
        
        if (filter == null) {
            filter = new ReadFilter();
        }
        
        HttpProvider httpProvider;
        if (filter.getLinkUri() == null) {
            httpProvider = getHttpProvider(parameterProvider.getParameters(filter));
        } else {
            httpProvider = getHttpProvider(filter.getLinkUri());
        }
        ReadPipeWorker<List<T>> worker = new ReadPipeWorker<List<T>>(httpProvider, uuid, filter, dataRoot, gson, this, pageConfig, encoding, klass);

        
        worker.registerCallback(resultCallback);
        worker.registerCallback((Callback<Pair<Serializable, List<T>>>)new RemoveCallback(uuid));

        runningWorkers.put(uuid, worker);

        worker.run();

        return uuid;
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable read(final Callback<Pair<Serializable, List<T>>> callback) {
        return readWithFilter(null, callback);
    }

    @Override
    public Serializable save(final T data, final Callback<Pair<Serializable, T>> resultCallback) {
        final String id;

        try {
            String recordIdFieldName = Scan.recordIdFieldNameIn(data.getClass());
            Object result = new Property(data.getClass(), recordIdFieldName).getValue(data);
            id = result == null ? null : result.toString();
        } catch (Exception e) {
            resultCallback.onFailure(e);
            return null;
        }

        Serializable uuid = ID_GENERATOR.generate();
        SavePipeWorker<T> worker = new SavePipeWorker<T>(getHttpProvider(), uuid, id, gson, encoding, klass);

        worker.registerCallback(resultCallback);
        worker.registerCallback(new RemoveCallback(uuid));


        runningWorkers.put(uuid, worker);

        worker.run();

        return uuid;


    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable remove(final String id, Callback<Pair<Serializable, T>> resultCallback) {

        Serializable uuid = ID_GENERATOR.generate();
        RestPipeWorker<T> worker = new DeletePipeWorker<T>(getHttpProvider(), uuid, id);

        worker.registerCallback(resultCallback);
        worker.registerCallback(new RemoveCallback(uuid));


        runningWorkers.put(uuid, worker);
        worker.run();
        return uuid;

    }


    private URL appendQuery(String query, URL baseURL) {
        try {
            URI baseURI = baseURL.toURI();
            String baseQuery = baseURI.getQuery();
            if (baseQuery == null || baseQuery.isEmpty()) {
                baseQuery = query;
            } else {
                if (query != null && !query.isEmpty()) {
                    baseQuery = baseQuery + "&" + query;
                }
            }

            return new URI(baseURI.getScheme(), baseURI.getUserInfo(), baseURI.getHost(), baseURI.getPort(), baseURI.getPath(), baseQuery, baseURI.getFragment()).toURL();
        } catch (MalformedURLException ex) {
            Log.e(TAG, "The URL could not be created from " + baseURL.toString(), ex);
            throw new RuntimeException(ex);
        } catch (URISyntaxException ex) {
            Log.e(TAG, "Error turning " + query + " into URI query.", ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void setAuthenticationModule(AuthenticationModule module) {
        this.authModule = module;
    }

    /**
     * Apply authentication if the token is present
     */
    private AuthorizationFields loadAuth() {

        if (authModule != null && authModule.isLoggedIn()) {
            return authModule.getAuthorizationFields();
        }

        return new AuthorizationFields();
    }

    /**
     * Sets the encoding of the Pipe. May not be null.
     *
     * @param encoding
     * @throws IllegalArgumentException if encoding is null
     */
    public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }

    /**
     *
     * @param queryParameters
     * @return a url with query params added
     */
    private URL addAuthorization(List<Pair<String, String>> queryParameters, URL baseURL) {

        StringBuilder queryBuilder = new StringBuilder();

        String amp = "";
        for (Pair<String, String> parameter : queryParameters) {
            try {
                queryBuilder.append(amp)
                        .append(URLEncoder.encode(parameter.first, "UTF-8"))
                        .append("=")
                        .append(URLEncoder.encode(parameter.second, "UTF-8"));

                amp = "&";
            } catch (UnsupportedEncodingException ex) {
                Log.e(TAG, "UTF-8 encoding is not supported.", ex);
                throw new RuntimeException(ex);

            }
        }

        return appendQuery(queryBuilder.toString(), baseURL);

    }

    private void addAuthHeaders(HttpProvider httpProvider, AuthorizationFields fields) {
        List<Pair<String, String>> authHeaders = fields.getHeaders();

        for (Pair<String, String> header : authHeaders) {
            httpProvider.setDefaultHeader(header.first, header.second);
        }

    }

    private HttpProvider getHttpProvider() {
        return getHttpProvider(URI.create(""));
    }

    private HttpProvider getHttpProvider(URI relativeUri) {
        try {
            AuthorizationFields fields = loadAuth();

            URL authorizedURL = addAuthorization(fields.getQueryParameters(), URIUtils.resolve(baseURL.toURI(), relativeUri).toURL());

            final HttpProvider httpProvider = httpProviderFactory.get(authorizedURL);
            addAuthHeaders(httpProvider, fields);
            return httpProvider;
        } catch (MalformedURLException ex) {
            Log.e(TAG, "error resolving " + baseURL + " with " + relativeUri, ex);
            throw new RuntimeException(ex);
        } catch (URISyntaxException ex) {
            Log.e(TAG, "error resolving " + baseURL + " with " + relativeUri, ex);
            throw new RuntimeException(ex);
        }
    }


    public String getDataRoot() {
        return dataRoot;
    }

    protected void setDataRoot(String dataRoot) {
        this.dataRoot = dataRoot;
    }

    
    public ParameterProvider getParameterProvider() {
        return parameterProvider;
    }

    protected void setParameterProvider(ParameterProvider parameterProvider) {
        this.parameterProvider = parameterProvider;
    }


    @Override
    public PipeWorker<T> getWorker(Serializable id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private class RemoveCallback implements Callback<Pair<Serializable, T>> {

        private final Serializable id;

        public RemoveCallback(final Serializable id) {
            this.id = id;
        }

        @Override
        public void onSuccess(Pair<Serializable, T> data) {
            finishedWorkers.put(id, runningWorkers.remove(id));
        }

        @Override
        public void onFailure(Exception e) {
            finishedWorkers.put(id, runningWorkers.remove(id));
        }
    };
}
