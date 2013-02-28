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
package org.jboss.aerogear.android.authentication;

import java.util.Map;
import org.jboss.aerogear.android.http.HeaderAndBody;


public interface AuthenticationModuleHandler {
    
    /**
     * @param userData
     */
    public HeaderAndBody onEnroll(Map<String, String> userData);

    /**
     * @param username
     * @param password
     */
    public HeaderAndBody onLogin(String username, String password);

    /**
     * Performs a logout of the current user.
     * <p/>
     */
    public void onLogout();
}
