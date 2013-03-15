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

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.jboss.aerogear.android.authentication.AuthenticationConfig;
import org.jboss.aerogear.android.authentication.AuthenticationModule;
import org.jboss.aerogear.android.authentication.impl.loader.ModernAuthenticationModuleAdapter;
import org.jboss.aerogear.android.authentication.impl.loader.support.SupportAuthenticationModuleAdapter;

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
     * Removes a AuthenticationModule for name
     * 
     * @param name
     * @return a AuthenticationModule for name or null if there isn't a value for name
     * @throws NullPointerException is name is null
     */
    public AuthenticationModule remove(String name) {
        return modules.remove(name);
    }

    /**
     * 
     * Builds a AuthenticationModule based on the AuthenticationConfig and 
     * records it as name
     * 
     * @param name
     * @param config
     * @return a fully operational AuthenticationModule
     * @throws NullPointerException is config or name is null
     */
    public AuthenticationModule auth(String name, AuthenticationConfig config) {

        if (!AuthTypes.AG_SECURITY.equals(config.getAuthType())) {

            throw new IllegalArgumentException("Unsupported Auth Type passed");
        }
        modules.put(name, new AGSecurityAuthenticationModule(baseURL, config));
        return modules.get(name);

    }

    public void add(String name, AuthenticationModule module) {
        modules.put(name, module);
    }

    /**
     * Gets a AuthenticationModule for name
     * 
     * This method should NOT be called by Activities or Fragments.  
     * This method is safe for Services, tests, etc.
     * 
     * @param name
     * 
     * @return a AuthenticationModule for name or null if there isn't a value for name
     * @throws NullPointerException is name is null
     */
    public AuthenticationModule get(String name) {
        return modules.get(name);
    }

    /**
     * Gets a AuthenticationModule for name. This will wrap the module in a Loader.
     * 
     * @param name
     * @param activity the activity which the Loaders should be bound against.
     * 
     * @return a ModernAuthenticationModuleAdapter for name
     * @throws NullPointerException is name is null
     */
    public AuthenticationModule get(String name, Activity activity) {
        return new ModernAuthenticationModuleAdapter(activity, modules.get(name), name);
    }

    /**
     * Gets a AuthenticationModule for name. This will wrap the module in a Loader.
     * 
     * @param name
     * @param fragment the fragment the Loaders will be bound against.
     * @param applicationContext 
     * 
     * @return a ModernAuthenticationModuleAdapter for name
     * @throws NullPointerException is name is null
     */
    public AuthenticationModule get(String name, Fragment fragment, Context applicationContext) {
        return new ModernAuthenticationModuleAdapter(fragment, applicationContext, modules.get(name), name);
    }

    /**
     * Gets a AuthenticationModule for name. This will wrap the module in a Loader.
     * 
     * @param name
     * @param activity the activity which the Loaders should be bound against.
     * 
     * @return a SupportAuthenticationModuleAdapter for name
     * @throws NullPointerException is name is null
     */
    public AuthenticationModule get(String name, FragmentActivity activity) {
        return new SupportAuthenticationModuleAdapter(activity, modules.get(name), name);
    }

    /**
     * Gets a AuthenticationModule for name.  This will wrap the module in a Loader.
     * 
     * @param name
     * @param fragment the fragment the Loaders will be bound against.
     * @param applicationContext 
     *
     * @return a SupportAuthenticationModuleAdapter for name
     * @throws NullPointerException is name is null
     */
    public AuthenticationModule get(String name, android.support.v4.app.Fragment fragment, Context applicationContext) {
        return new SupportAuthenticationModuleAdapter(fragment, applicationContext, modules.get(name), name);
    }

}
