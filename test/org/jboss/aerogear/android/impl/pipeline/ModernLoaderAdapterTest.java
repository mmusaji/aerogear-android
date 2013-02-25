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

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.Provider;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.authentication.AuthorizationFields;
import org.jboss.aerogear.android.http.HeaderAndBody;
import org.jboss.aerogear.android.http.HttpProvider;
import org.jboss.aerogear.android.impl.core.HttpProviderFactory;
import org.jboss.aerogear.android.impl.helper.Data;
import org.jboss.aerogear.android.impl.helper.UnitTestUtils;
import org.jboss.aerogear.android.impl.http.HttpStubProvider;
import org.jboss.aerogear.android.impl.util.StubActivity;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Point;

import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;


@RunWith(RobolectricTestRunner.class)
public class ModernLoaderAdapterTest {
    
    private static final String TAG = RestAdapterTest.class.getSimpleName();
    private static final String SERIALIZED_POINTS = "{\"points\":[{\"x\":0,\"y\":0},{\"x\":1,\"y\":2},{\"x\":2,\"y\":4},{\"x\":3,\"y\":6},{\"x\":4,\"y\":8},{\"x\":5,\"y\":10},{\"x\":6,\"y\":12},{\"x\":7,\"y\":14},{\"x\":8,\"y\":16},{\"x\":9,\"y\":18}],\"id\":\"1\"}";
    private URL url;
    private final Provider<HttpProvider> stubHttpProviderFactory = new Provider<HttpProvider>() {
        @Override
        public HttpProvider get(Object... in) {
            return new HttpStubProvider((URL) in[0]);
        }
    };

    @Before
    public void setup() throws MalformedURLException {
        url = new URL("http://server.com/context/");
    }
    
     @Test
    public void testSingleObjectRead() throws Exception {
         
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Point.class, new PointTypeAdapter());
        HeaderAndBody response = new HeaderAndBody(SERIALIZED_POINTS.getBytes(), new HashMap<String, Object>());
        final HttpStubProvider provider = new HttpStubProvider(url, response);
        PipeConfig config = new PipeConfig(url, RestAdapterTest.ListClassId.class);
        config.setGsonBuilder(builder);
        RestAdapter<RestAdapterTest.ListClassId> restPipe = new RestAdapter<RestAdapterTest.ListClassId>(RestAdapterTest.ListClassId.class, url, config);
        Object restRunner = UnitTestUtils.getPrivateField(restPipe, "restRunner");
        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory", new Provider<HttpProvider>() {
            @Override
            public HttpProvider get(Object... in) {
                return provider;
            }
        });
        
        Activity activity = new StubActivity();
        
        ModernLoaderAdapter<RestAdapterTest.ListClassId> adapter = new ModernLoaderAdapter<RestAdapterTest.ListClassId>(activity, restPipe, builder.create());
        
        List<RestAdapterTest.ListClassId> result = runRead(adapter);

        List<Point> returnedPoints = result.get(0).points;
        Assert.assertEquals(10, returnedPoints.size());

    }
     
     private <T> List<T> runRead(Pipe<T> restPipe) throws InterruptedException {
        return runRead(restPipe, null);
    }

    /**
     * Runs a read method, returns the result of the call back and makes sure no
     * exceptions are thrown
     *
     * @param restPipe
     */
    private <T> List<T> runRead(Pipe<T> restPipe, ReadFilter readFilter) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean hasException = new AtomicBoolean(false);
        final AtomicReference<List<T>> resultRef = new AtomicReference<List<T>>();

        restPipe.readWithFilter(readFilter, new Callback<List<T>>() {
            @Override
            public void onSuccess(List<T> data) {
                resultRef.set(data);
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                hasException.set(true);
                Logger.getLogger(RestAdapterTest.class.getSimpleName()).log(Level.SEVERE, e.getMessage(), e);
                latch.countDown();
            }
        });

        latch.await(2, TimeUnit.SECONDS);
        Assert.assertFalse(hasException.get());

        return resultRef.get();
    }

    @Test
    public void runReadWithFilterAndAuthenticaiton() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);

        HttpProviderFactory factory = mock(HttpProviderFactory.class);
        when(factory.get(anyObject())).thenReturn(mock(HttpProvider.class));

        AuthorizationFields authFields = new AuthorizationFields();
        authFields.addQueryParameter("token", "token");

        AuthenticationModule urlModule = mock(AuthenticationModule.class);
        when(urlModule.isLoggedIn()).thenReturn(true);
        when(urlModule.getAuthorizationFields()).thenReturn(authFields);

        PipeConfig config = new PipeConfig(url, Data.class);
        config.setAuthModule(urlModule);
        
        RestAdapter<Data> pipe = new RestAdapter<Data>(Data.class, url, config);
        Object restRunner = UnitTestUtils.getPrivateField(pipe, "restRunner");

        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory", factory);

        ReadFilter filter = new ReadFilter();
        filter.setLimit(10);
        filter.setWhere(new JSONObject("{\"model\":\"BMW\"}"));

        

        StubActivity stubActivity = new StubActivity();
        stubActivity.onCreate(null);
        ModernLoaderAdapter<Data> adapter = new ModernLoaderAdapter<Data>(stubActivity, pipe, pipe.getGson());
        
        adapter.readWithFilter(filter, new Callback<List<Data>>() {
            @Override
            public void onSuccess(List<Data> data) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
                Logger.getLogger(getClass().getSimpleName()).log(Level.SEVERE, TAG, e);
            }
        });
        latch.await(500, TimeUnit.MILLISECONDS);

        verify(factory).get(new URL(url.toString() + "?limit=10&where=%7B%22model%22:%22BMW%22%7D&token=token"));
    }
    
    /**
     * Runs a read method, returns the result of the call back and rethrows the
     * underlying exception
     *
     * @param restPipe
     */
    private <T> List<T> runReadForException(Pipe<T> restPipe, ReadFilter readFilter) throws InterruptedException, Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean hasException = new AtomicBoolean(false);
        final AtomicReference<Exception> exceptionref = new AtomicReference<Exception>();
        restPipe.readWithFilter(readFilter, new Callback<List<T>>() {
            @Override
            public void onSuccess(List<T> data) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                hasException.set(true);
                exceptionref.set(e);
                latch.countDown();
            }
        });

        latch.await(2, TimeUnit.SECONDS);
        Assert.assertTrue(hasException.get());

        throw exceptionref.get();
    }

    private static class PointTypeAdapter implements InstanceCreator, JsonSerializer, JsonDeserializer {

        @Override
        public Object createInstance(Type type) {
            return new Point();
        }

        @Override
        public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("x", ((Point) src).x);
            object.addProperty("y", ((Point) src).y);
            return object;
        }

        @Override
        public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new Point(json.getAsJsonObject().getAsJsonPrimitive("x").getAsInt(),
                    json.getAsJsonObject().getAsJsonPrimitive("y").getAsInt());
        }
    }
    
}
