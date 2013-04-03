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

import org.jboss.aerogear.android.authentication.impl.AuthTypes;

/**
 * This is the minimum (hopefully) necessary parameters for an
 * {@link AuthenticationModule}
 */
public class AuthenticationConfig {

    private String loginEndpoint = "/auth/login";
    private String logoutEndpoint = "/auth/logout";
    private String enrollEndpoint = "/auth/enroll";
    
    private AuthType authType = AuthTypes.AG_SECURITY;

    private Integer timeout = Integer.MAX_VALUE;
    
    public String getLoginEndpoint() {
        return loginEndpoint;
    }

    public void setLoginEndpoint(String loginEndpoint) {
        this.loginEndpoint = loginEndpoint;
    }

    public String getLogoutEndpoint() {
        return logoutEndpoint;
    }

    public void setLogoutEndpoint(String logoutEndpoint) {
        this.logoutEndpoint = logoutEndpoint;
    }

    public String getEnrollEndpoint() {
        return enrollEndpoint;
    }

    public void setEnrollEndpoint(String enrollEndpoint) {
        this.enrollEndpoint = enrollEndpoint;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    /**
     * Timeout is the length of time in milliseconds that a Pipe will wait for a
     * response from a call to read, readWithfilter, save or remove
     *
     * @return the current timeout.
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * Timeout is the length of time in milliseconds that a Pipe will wait for a
     * response from a call to read, readWithfilter, save or remove
     *
     * @param timeout a new
     */
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
    
}
