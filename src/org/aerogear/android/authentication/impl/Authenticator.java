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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.aerogear.android.authentication.AuthenticationConfig;
import org.aerogear.android.authentication.AuthenticationModule;

/**
 * This is the default implementation of Authenticator.
 * It uses a HashMap behind the scenes to store its modules.
 * <p/>
 * As a note, you should NOT extend this class for production or application
 * purposes.  This class is made non-final ONLY for testing/mocking/academic
 * purposes.
 */
public class Authenticator {

	private final Map<String, AuthenticationModule> modules = new HashMap<String, AuthenticationModule>();
	private final URL baseURL;

    public Authenticator(URL baseURL) {
        this.baseURL = baseURL;
    }

    public Authenticator(String baseURL) {
        try {
            this.baseURL = new URL(baseURL);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }

    }

    /**
     * {@inheritDoc }
     */

    public AuthenticationModule get(String name) {
        return modules.get(name);
    }

    /**
     * {@inheritDoc }
     */

    public AuthenticationModule remove(String name) {
        return modules.remove(name);
    }

    /**
     * {@inheritDoc }
     */
    public AuthenticationModule auth(String name, AuthenticationConfig config) {

        assert config != null;

        if (!AuthTypes.REST.equals(config.getAuthType())) {

            throw new IllegalArgumentException("Unsupported Auth Type passed");
        }
        modules.put(name, new RestAuthenticationModule(baseURL, config));
        return modules.get(name);

    }

}
