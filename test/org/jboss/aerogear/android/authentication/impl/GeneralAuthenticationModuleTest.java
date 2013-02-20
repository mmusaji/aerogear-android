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

package org.jboss.aerogear.android.authentication.impl;

import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.authentication.AbstractAuthenticationModule;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.authentication.AuthorizationFields;
import org.jboss.aerogear.android.http.HttpProvider;
import org.jboss.aerogear.android.impl.core.HttpProviderFactory;
import org.jboss.aerogear.android.impl.helper.Data;
import org.jboss.aerogear.android.impl.helper.UnitTestUtils;
import org.jboss.aerogear.android.impl.pipeline.RestAdapter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class GeneralAuthenticationModuleTest implements AuthenticationModuleTest {

    private static final URL SIMPLE_URL;

    static {
        try {
            SIMPLE_URL = new URL("http://localhost:8080/todo-server");
        } catch (MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Test
    public void applySecurityTokenOnURL() throws Exception {

        HttpProviderFactory factory = mock(HttpProviderFactory.class);
        when(factory.get(anyObject())).thenReturn(mock(HttpProvider.class));

        AuthorizationFields authFields = new AuthorizationFields();
        authFields.addQueryParameter("token", TOKEN);

        AuthenticationModule urlModule = mock(AuthenticationModule.class);
        when(urlModule.isLoggedIn()).thenReturn(true);
        when(urlModule.getAuthorizationFields()).thenReturn(authFields);

        RestAdapter<Data> adapter = new RestAdapter<Data>(Data.class, SIMPLE_URL);
        Object restRunner = UnitTestUtils.getPrivateField(adapter, "restRunner");
        UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory", factory);
        adapter.setAuthenticationModule(urlModule);

        adapter.read(new Callback<List<Data>>() {

            @Override
            public void onSuccess(List<Data> data) {
            }

            @Override
            public void onFailure(Exception e) {
            }
        });

        verify(factory).get(new URL(SIMPLE_URL.toString() + "?token=" + TOKEN));
    }

    @Test(timeout = 1000l)
    public void testAbstractMethodsThrowExceptions() throws InterruptedException {
        AuthenticationModule module = mock(AbstractAuthenticationModule.class, CALLS_REAL_METHODS);
        final CountDownLatch latch = new CountDownLatch(3);
        Callback throwIfSuccess = new Callback() {

            @Override
            public void onSuccess(Object data) {
                Assert.assertTrue("This should not be called", false);
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
            }
        };
        module.enroll(new HashMap<String, String>(), throwIfSuccess);
        module.login("username", "password", throwIfSuccess);
        module.logout(throwIfSuccess);

        latch.await();

    }

}
