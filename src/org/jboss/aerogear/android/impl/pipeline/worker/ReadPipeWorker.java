/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.aerogear.android.impl.pipeline.worker;

import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.http.HttpProvider;
import org.jboss.aerogear.android.impl.pipeline.RestAdapter;
import org.jboss.aerogear.android.impl.pipeline.RestPipeWorker;
import org.jboss.aerogear.android.impl.pipeline.paging.WebLink;
import org.jboss.aerogear.android.impl.pipeline.paging.WrappingPagedList;
import org.jboss.aerogear.android.impl.util.ParseException;
import org.jboss.aerogear.android.impl.util.WebLinkParser;
import org.jboss.aerogear.android.pipeline.paging.PageConfig;
import org.json.JSONObject;

/**
 *
 * @author summers
 */
public class ReadPipeWorker<T> extends RestPipeWorker<T> {

    private final Gson gson;
    private static final String TAG = DeletePipeWorker.class.getSimpleName();
    private final Charset encoding;
    private final Class<T> klass;
    private final ReadFilter filter;
    private final String dataRoot;
    private final Class<T[]> arrayKlass;
    private final PageConfig pageConfig;
    private final RestAdapter adapter;

    public ReadPipeWorker(HttpProvider httpProvider, Serializable uuid, ReadFilter filter, String dataRoot, Gson gson, RestAdapter adapter, PageConfig pageConfig, Charset encoding, Class klass) {
        super(httpProvider, uuid);
        this.arrayKlass = asArrayClass(klass);
        this.filter = filter;
        this.gson = gson;
        this.encoding = encoding;
        this.klass = klass;
        this.dataRoot = dataRoot;
        this.pageConfig = pageConfig;
        this.adapter = adapter;
    }

    @Override
    public void run() {
        final ReadFilter innerFilter;
        if (filter == null) {
            innerFilter = new ReadFilter();
        } else {
            innerFilter = filter;
        }

        new AsyncTask<Void, Void, Void>() {
            List<T> result = null;
            Exception exception = null;

            @Override
            protected Void doInBackground(Void... voids) {
                try {

                    HeaderAndBody httpResponse = getHttpProvider().get();
                    byte[] responseBody = httpResponse.getBody();
                    String responseAsString = new String(responseBody, encoding);
                    JsonParser parser = new JsonParser();
                    JsonElement httpJsonResult = parser.parse(responseAsString);
                    httpJsonResult = getResultElement(httpJsonResult, dataRoot);
                    if (httpJsonResult.isJsonArray()) {
                        T[] resultArray = gson.fromJson(httpJsonResult, arrayKlass);
                        this.result = Arrays.asList(resultArray);
                        if (pageConfig != null) {
                            this.result = computePagedList(this.result, httpResponse, innerFilter.getWhere());
                        }
                    } else {
                        T resultObject = gson.fromJson(httpJsonResult, klass);
                        List<T> resultList = new ArrayList<T>(1);
                        resultList.add(resultObject);
                        this.result = resultList;
                        if (pageConfig != null) {
                            this.result = computePagedList(this.result, httpResponse, innerFilter.getWhere());
                        }
                    }
                } catch (Exception e) {
                    exception = e;
                }
                return null;
            }

           @Override
            protected void onPostExecute(Void ignore) {
                super.onPostExecute(ignore);
                //lets not change callbacks after we finish.
                final Set<Callback> callbacks = Collections.unmodifiableSet(ReadPipeWorker.super.callbacks);
        
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

    private JsonElement getResultElement(JsonElement element, String dataRoot) {
        String[] identifiers = dataRoot.split("\\.");
        for (String identifier : identifiers) {
            JsonElement newElement = element.getAsJsonObject().get(identifier);
            if (newElement == null) {
                return element;
            } else {
                element = newElement;
            }
        }
        return element;
    }

    /**
     * This will return a class of the type T[] from a given class. When we read
     * from the AG pipe, Java needs a reference to a generic array type.
     *
     * @param klass
     * @return an array of klass with a length of 1
     */
    private Class<T[]> asArrayClass(Class<T> klass) {

        return (Class<T[]>) Array.newInstance(klass, 1).getClass();
    }

    /**
     *
     * This method checks for paging information and returns the appropriate
     * data
     *
     * @param result
     * @param httpResponse
     * @param where
     * @return a {@link WrappingPagedList} if there is paging, result if not.
     */
    private List<T> computePagedList(List<T> result, HeaderAndBody httpResponse, JSONObject where) {
        ReadFilter previousRead = null;
        ReadFilter nextRead = null;

        if (PageConfig.MetadataLocations.WEB_LINKING.equals(pageConfig.getMetadataLocation())) {
            String webLinksRaw = "";
            final String relHeader = "rel";
            final String nextIdentifier = pageConfig.getNextIdentifier();
            final String prevIdentifier = pageConfig.getPreviousIdentifier();
            try {
                webLinksRaw = getWebLinkHeader(httpResponse);
                if (webLinksRaw == null) { //no paging, return result
                    return result;
                }
                List<WebLink> webLinksParsed = WebLinkParser.parse(webLinksRaw);
                for (WebLink link : webLinksParsed) {
                    if (nextIdentifier.equals(link.getParameters().get(relHeader))) {
                        nextRead = new ReadFilter();
                        nextRead.setLinkUri(new URI(link.getUri()));
                    } else if (prevIdentifier.equals(link.getParameters().get(relHeader))) {
                        previousRead = new ReadFilter();
                        previousRead.setLinkUri(new URI(link.getUri()));
                    }

                }
            } catch (URISyntaxException ex) {
                Log.e(TAG, webLinksRaw + " did not contain a valid context URI", ex);
                throw new RuntimeException(ex);
            } catch (ParseException ex) {
                Log.e(TAG, webLinksRaw + " could not be parsed as a web link header", ex);
                throw new RuntimeException(ex);
            }
        } else if (pageConfig.getMetadataLocation().equals(PageConfig.MetadataLocations.HEADERS)) {
            nextRead = pageConfig.getPageHeaderParser().getNextFilter(httpResponse, pageConfig);
            previousRead = pageConfig.getPageHeaderParser().getPreviousFilter(httpResponse, pageConfig);
        } else if (pageConfig.getMetadataLocation().equals(PageConfig.MetadataLocations.BODY)) {
            nextRead = pageConfig.getPageHeaderParser().getNextFilter(httpResponse, pageConfig);
            previousRead = pageConfig.getPageHeaderParser().getPreviousFilter(httpResponse, pageConfig);
        } else {
            throw new IllegalStateException("Not supported");
        }
        if (nextRead != null) {
            nextRead.setWhere(where);
        }

        if (previousRead != null) {
            previousRead.setWhere(where);
        }

        return new WrappingPagedList<T>(adapter, result, nextRead, previousRead);
    }

    private String getWebLinkHeader(HeaderAndBody httpResponse) {
        String linkHeaderName = "Link";
        Object header = httpResponse.getHeader(linkHeaderName);
        if (header != null) {
            return header.toString();
        }
        return null;
    }
}
