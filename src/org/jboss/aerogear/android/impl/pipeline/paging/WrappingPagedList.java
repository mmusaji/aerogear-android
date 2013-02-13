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
package org.jboss.aerogear.android.impl.pipeline.paging;

import android.util.Pair;
import com.google.common.collect.ForwardingList;
import java.io.Serializable;
import java.util.List;
import org.jboss.aerogear.android.Callback;
import org.jboss.aerogear.android.ReadFilter;
import org.jboss.aerogear.android.pipeline.Pipe;
import org.jboss.aerogear.android.pipeline.paging.PagedList;

/**
 * Wraps a resultSet in a ForwardingList and provides paging methods.
 * 
 * This class also combines 
 * 
 */
public class WrappingPagedList<T> extends ForwardingList<T> implements PagedList<T> {

    private final Pipe<T> pipe;
    private final List<T> data;
    private final ReadFilter nextFilter;
    private final ReadFilter previousFilter;

    /**
     * @param pipe
     * @param data
     * @param nextFilter
     * @param previousFilter 
     */
    public WrappingPagedList(Pipe<T> pipe, List<T> data, ReadFilter nextFilter, ReadFilter previousFilter) {
        this.pipe = pipe;
        this.data = data;
        this.nextFilter = nextFilter;
        this.previousFilter = previousFilter;
    }

    @Override
    protected List<T> delegate() {
        return data;
    }

    @Override
    public void next(Callback<Pair<Serializable, List<T>>> callback) {
        pipe.readWithFilter(nextFilter, callback);
    }

    @Override
    public void previous(Callback<Pair<Serializable, List<T>>> callback) {
        pipe.readWithFilter(previousFilter, callback);
    }

    public ReadFilter getNextFilter() {
        return nextFilter;
    }

    public ReadFilter getPreviousFilter() {
        return previousFilter;
    }

}
