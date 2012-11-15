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

import com.xtremelabs.robolectric.shadows.StatusLineStub;
import com.xtremelabs.robolectric.tester.org.apache.http.RequestMatcher;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.aerogear.android.Callback;
import org.aerogear.android.core.HeaderAndBody;

/**
 * This interface sets up all of the static values for 
 * {@link RestAuthenticationModuleTest}
 */
public interface AuthenticationModuleTest {

    /**
     * Default AUTH Token
     */
    static final String TOKEN = "a016b29b-da74-4833-aa50-43c55788c528";

    static final String PASSING_USERNAME = "spittman";
    static final String FAILING_USERNAME = "fail";
    static final String LOGIN_PASSWORD = "password";
    static final String ENROLL_PASSWORD = "spittman";
    
    
    final class SimpleCallback implements Callback<HeaderAndBody> {

        HeaderAndBody data;
        Exception exception;

        @Override
        public void onSuccess(HeaderAndBody data) {
            this.data = data;
        }

        @Override
        public void onFailure(Exception e) {
            this.exception = e;
        }
    }

    final class VoidCallback implements Callback<Void> {

        Exception exception;

        @Override
        public void onSuccess(Void data) {

        }

        @Override
        public void onFailure(Exception e) {
            this.exception = e;
        }
    }
}
