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

package org.aerogear.android.pipeline;

import java.net.URL;
import java.util.List;
import org.aerogear.android.Callback;
import org.aerogear.android.authentication.AuthenticationModule;

/**
 * A {@link Pipe} represents a server connection. An object of this class is responsible to communicate
 * with the server in order to perfom read/write operations.
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
     * @param callback The callback for consuming the result from the {@link Pipe} invocation.
     */
    void read(Callback<List<T>> callback);

    /**
     * Saves or updates a given object on the server.
     *
     * @param item     the item to save or update
     * @param callback The callback for consuming the result from the {@link Pipe} invocation.
     */
    void save(T item, Callback<T> callback);

    /**
     * Removes an object from the underlying server connection. The given key argument is used as the objects ID.
     *
     * @param id       representing the ‘id’ of the object to be removed
     * @param callback The callback for consuming the result from the {@link Pipe} invocation.
     */
    void remove(String id, Callback<Void> callback);

    /**
     * Sets the authentication module for the Pipe.
     * It should already be logged in.
     *
     * @param module
     */
    void setAuthenticationModule(AuthenticationModule module);

}
