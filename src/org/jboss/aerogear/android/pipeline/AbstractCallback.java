/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and
 * individual contributors by the
 *
 * @authors tag. See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.jboss.aerogear.android.pipeline;

import com.google.common.base.Objects;
import org.jboss.aerogear.android.Callback;

/**
 * This class provides a hashcode method for a callback based on constructor
 * parameters. This callback is meant to be used in conjuction with
 * Activities/Fragments and LoaderPipes.
 *
 * Using this class instead of an anonymous Callback will allow your application
 * to persist results from loaders through configuration changes.
 */
public abstract class AbstractCallback<T> implements Callback<T> {

    private static final long serialVersionUID = 2336090029150712942L;
    final int hashcode;

    /**
     * This accepts an arbitrary list of Object and uses {@link Objects} to 
     * generate a hashcode.  This code is used to provided the loader manager
     * with a unique value to determine uniqueness of calls to read, etc.
     * 
     * @param params 
     */
    public AbstractCallback(Object... params) {
        hashcode = Objects.hashCode(params);
    }

    @Override
    public int hashCode() {
        return hashcode;
    }
}
