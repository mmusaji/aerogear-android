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

package org.jboss.aerogear.android.pipeline;

import android.util.Pair;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.authentication.AuthenticationModule;

/**
 * A {@link Pipe} represents a server connection. An object of this class is responsible to communicate
 * with the server in order to perform read/write operations.
 *
 * @param <T> The data type of the {@link Pipe} operation
 */
public interface Pipe<T> {

    /**
     * Returns the connection type of this {@link Pipe} object (e.g. <code>REST</code>).
     *
     * @return the connection type
     */
    PipeType getType();

    /**
     * Returns the {@link URL} to which this {@link Pipe} object points.
     *
     * @return the endpoint URL
     */
    URL getUrl();

    /**
     * Reads all the data from the underlying server connection.
     *
     * @param resultCallback  default callback the PipeWorker will return to.
     * @return an identifier for a PipeWorker
     * 
     */
    Serializable read(Callback<Pair<Serializable, List<T>>> resultCallback);

    /**
     * Reads all the data from the underlying server connection.
     *
     * @param filter a {@link ReadFilter} for performing pagination and querying.
     * @param resultCallback  default callback the PipeWorker will return to.
     * @return an identifier for a PipeWorker
     */
    Serializable readWithFilter(ReadFilter filter, Callback<Pair<Serializable, List<T>>> resultCallback);

    /**
     * Saves or updates a given object on the server.
     *
     * @param item     the item to save or update
     * @param resultCallback  default callback the PipeWorker will return to.
     * 
     * @return an identifier for a PipeWorker
     */
    Serializable save(T item, Callback<Pair<Serializable, List<T>>> resultCallback);

    /**
     * Removes an object from the underlying server connection. The given key argument is used as the objects ID.
     *
     * @param id       representing the ‘id’ of the object to be removed
     * @param resultCallback  default callback the PipeWorker will return to.
     * @return an identifier for a PipeWorker
     */
    Serializable remove(String id, Callback<Pair<Serializable, List<T>>> resultCallback);

    /**
     * Sets the authentication module for the Pipe.
     * It should already be logged in.
     *
     * @param module
     */
    void setAuthenticationModule(AuthenticationModule module);

    PipeWorker<T> getWorker(Serializable id);
}
