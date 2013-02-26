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

import com.google.common.collect.Multimap;

/**
 * Sometimes a Pipe will actually be wrapped in a Loader.  Classes which do so 
 * implement this interface and have certain methods like reset exposed.
 */
public interface LoaderPipe<T> extends Pipe<T> {
    
    /**
     * Calls reset on all loaders associated with this pipe.
     */
    public void reset();

    /**
     * Passes in a multimap of ids for the named pipe.  LoaderPipe should manage this collection.
     * 
     * @param idsForNamedPipes 
     */
    public void setLoaderIds(Multimap<String, Integer> idsForNamedPipes);
    
}
