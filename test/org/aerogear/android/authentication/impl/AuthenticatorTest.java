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

import com.xtremelabs.robolectric.RobolectricTestRunner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aerogear.android.authentication.AuthenticationModule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RobolectricTestRunner.class)
public class AuthenticatorTest {
    
    private static final URL SIMPLE_URL;
    private static String SIMPLE_MODULE_NAME  = "simple";
    
    static {
        try {
            SIMPLE_URL = new URL("http", "localhost", 80, "/");
        } catch (MalformedURLException ex) {
            Logger.getLogger(AuthenticatorTest.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
    
    @Test
    public void testAddSimpleAuthenticator() {

        Authenticator authenticator = new Authenticator(SIMPLE_URL);
        AuthenticationModule simpleAuthModule = authenticator.auth(SIMPLE_MODULE_NAME, new RestAuthenticationConfig());

        assertNotNull(simpleAuthModule);
        
    }
    
    @Test
    public void testAddAndGetSimpleAuthenticator() {
        Authenticator authenticator = new Authenticator(SIMPLE_URL);
        AuthenticationModule simpleAuthModule = authenticator.auth(SIMPLE_MODULE_NAME, new RestAuthenticationConfig());
        assertEquals(simpleAuthModule, authenticator.get(SIMPLE_MODULE_NAME));
    }

    
    @Test
    public void testAddAuthenticator() {

        Authenticator authenticator = new Authenticator(SIMPLE_URL);
        
        RestAuthenticationConfig config = new RestAuthenticationConfig();
        config.setAuthType(AuthTypes.REST);
        config.setEnrollEndpoint("testEnroill");
        
        AuthenticationModule simpleAuthModule = authenticator.auth(SIMPLE_MODULE_NAME, config);

        assertEquals(simpleAuthModule, authenticator.get(SIMPLE_MODULE_NAME));
    }

    
    @Test
    public void testGetNullAuthModule() {

        Authenticator authenticator = new Authenticator(SIMPLE_URL);

        assertNull(authenticator.get(SIMPLE_MODULE_NAME));
    }
}
