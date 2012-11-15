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
package org.aerogear.android.authentication.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.aerogear.android.Provider;
import org.aerogear.android.core.HeaderAndBody;
import org.aerogear.android.core.HttpException;
import org.aerogear.android.core.HttpProvider;
import org.aerogear.android.impl.core.HttpStubProvider;
import org.aerogear.android.util.TestUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RobolectricTestRunner.class)
public class RestAuthenticationModuleTest implements AuthenticationModuleTest {

    private static final String TAG = "RestAuthenticationModuleTest";
    private static final URL SIMPLE_URL;

    static {
        try {
            SIMPLE_URL = new URL("http://localhost:8080/todo-server");
        } catch (MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Before
    public void setup() {
        Robolectric.setDefaultHttpResponse(401, "Unauthorized");
    }

    @After
    public void cleaRules() {
        Robolectric.clearHttpResponseRules();
        Robolectric.clearPendingHttpResponses();
    }

    @Test(timeout = 50L)
    public void loginFails() throws Exception {
        RestAuthenticationModule module = new RestAuthenticationModule(SIMPLE_URL, new RestAuthenticationConfig());
        final CountDownLatch latch = new CountDownLatch(1);
        TestUtil.setPrivateField(module, "httpProviderProvider", new Provider<HttpProvider>() {
            @Override
            public HttpProvider get(Object... in) {
                return new HttpStubProvider(SIMPLE_URL) {
                    @Override
                    public HeaderAndBody post(String ignore) throws RuntimeException {
                        try {
                            throw new HttpException(new byte[1], 403);
                        } finally {
                            latch.countDown();
                        }
                    }
                };
            }
        });
        
        
        SimpleCallback callback = new SimpleCallback();
        module.login(PASSING_USERNAME, LOGIN_PASSWORD, callback);
        Assert.assertNotNull(callback.exception);
        Assert.assertFalse(module.isLoggedIn());
    }

    @Test()
    public void loginSucceeds() throws IOException, NoSuchFieldException, InterruptedException, IllegalArgumentException, IllegalAccessException {
        RestAuthenticationModule module = new RestAuthenticationModule(SIMPLE_URL, new RestAuthenticationConfig());
        final CountDownLatch latch = new CountDownLatch(1);
        TestUtil.setPrivateField(module, "httpProviderProvider", new Provider<HttpProvider>() {
            @Override
            public HttpProvider get(Object... in) {
                return new HttpStubProvider(SIMPLE_URL) {
                    @Override
                    public HeaderAndBody post(String ignore) throws RuntimeException {
                        try {
                            HashMap<String, Object> headers = new HashMap<String, Object>();
                            headers.put("Auth-Token", TOKEN);
                            return new HeaderAndBody(new byte[1], headers);
                        } finally {
                            latch.countDown();
                        }
                    }
                };
            }
        });


        SimpleCallback callback = new SimpleCallback();
        module.login(PASSING_USERNAME, LOGIN_PASSWORD, callback);
        latch.await();

        Assert.assertNull(callback.exception);
        Assert.assertNotNull(callback.data);
        Assert.assertTrue(module.isLoggedIn());
        Assert.assertEquals(TOKEN, module.getAuthToken());
    }
    
    @Test(timeout=5000L)
    public void enrollSucceeds() throws IOException {
        RestAuthenticationModule module = new RestAuthenticationModule(SIMPLE_URL, new RestAuthenticationConfig());
        Robolectric.addHttpResponseRule(ENROLL_PASS_MATCHER, ENROLL_PASS);
        SimpleCallback callback = new SimpleCallback();

        Map<String, String> userData = new HashMap<String, String>();
        userData.put("username", PASSING_USERNAME);
        userData.put("password", ENROLL_PASSWORD);
        userData.put("firstname", "Summers");
        userData.put("lastname", "Pittman");
        userData.put("role", "admin");

        module.enroll(userData, callback);

        while (!Robolectric.httpRequestWasMade()) {

        }
        ;
        Assert.assertNull(callback.exception);
        Assert.assertNotNull(callback.data);

        String result = new String(callback.data.getBody(), "UTF-8");
        JsonParser parser = new JsonParser();
        JsonObject resultObject = (JsonObject) parser.parse(result);
        Assert.assertEquals(PASSING_USERNAME, resultObject.get("username").getAsString());

        Assert.assertTrue(module.isLoggedIn());
        Assert.assertEquals(TOKEN, module.getAuthToken());
    }
    
    
    @Test(timeout=5000L)
    public void enrollFails() throws IOException {
        RestAuthenticationModule module = new RestAuthenticationModule(SIMPLE_URL, new RestAuthenticationConfig());
        Robolectric.addHttpResponseRule(ENROLL_FAIL_MATCHER, ENROLL_FAIL);
        SimpleCallback callback = new SimpleCallback();

        Map<String, String> userData = new HashMap<String, String>();
        userData.put("username", FAILING_USERNAME);
        userData.put("password", ENROLL_PASSWORD);
        userData.put("firstname", "Summers");
        userData.put("lastname", "Pittman");
        userData.put("role", "admin");

        module.enroll(userData, callback);

        while (!Robolectric.httpRequestWasMade()) {

        }
        ;
        Assert.assertNotNull(callback.exception);
        Assert.assertNull(callback.data);
        Assert.assertFalse(module.isLoggedIn());
        Assert.assertEquals(400, ((HttpException) callback.exception).getStatusCode());
    }
    
        
    @Test(timeout=5000L)
    public void logoutSucceeds() throws IOException {
        RestAuthenticationModule module = new RestAuthenticationModule(SIMPLE_URL, new RestAuthenticationConfig());
        Robolectric.addHttpResponseRule(LOGIN_MATCHER, VALID_LOGIN);
        SimpleCallback callback = new SimpleCallback();
        module.login(PASSING_USERNAME, LOGIN_PASSWORD, callback);

        Assert.assertNull(callback.exception);
        Assert.assertNotNull(callback.data);
        Assert.assertTrue(module.isLoggedIn());
        Assert.assertEquals(TOKEN, module.getAuthToken());

        //Reset
        Robolectric.clearHttpResponseRules();
        Robolectric.setDefaultHttpResponse(200, "");
        VoidCallback voidCallback = new VoidCallback();

        module.logout(voidCallback);
        Assert.assertNull(voidCallback.exception);

        Assert.assertFalse(module.isLoggedIn());
        Assert.assertEquals("", module.getAuthToken());

    }
}
