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

package org.aerogear.android.impl.datamanager;

import java.util.Collection;
import static org.aerogear.android.impl.datamanager.StoreTypes.MEMORY;
import org.aerogear.android.impl.helper.Data;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

public class MemoryStorageTest {

    private MemoryStorage<Data> store;

    @Before
    public void setup() {
        store = new MemoryStorage<Data>(new StubIdGenerator());
    }

    @Test
    public void testStoreType() {
        assertEquals("verifying the type", MEMORY, store.getType());
    }

    @Test
    public void testReadAll() {
        store.save(new Data("foo", "desc of foo"));
        store.save(new Data("bar", "desc of bar"));

        Collection<Data> datas = store.readAll();
        assertNotNull("datas could not be null", datas);
        assertEquals("datas should 2 data", 2, datas.size());
    }

    @Test()
    public void testRead() {
        store.save(new Data("foo", "desc of foo"));
        Data data = store.read(1);
        assertNotNull("data could not be null", data);
    }

    @Test
    public void testSave() {
        store.save(new Data("foo", "desc of foo"));
    }

    @Test
    public void testReset() {
        store.save(new Data("foo", "desc of foo"));
        store.save(new Data("bar", "desc of bar"));

        Data foo = store.read(1);
        assertNotNull("foo could not be null", foo);

        Data bar = store.read(2);
        assertNotNull("bar could not be null", bar);

        store.reset();

        foo = store.read(1);
        assertNull("foo should be null", foo);

        bar = store.read(2);
        assertNull("bar should be null", bar);
    }

    @Test
    public void testRemove() {
        store.save(new Data("foo", "desc of foo"));
        store.save(new Data("bar", "desc of bar"));

        Data foo = store.read(1);
        assertNotNull("foo could not be null", foo);

        Data bar = store.read(2);
        assertNotNull("bar could not be null", bar);

        store.remove(2);

        foo = store.read(1);
        assertNotNull("foo could not be null", foo);

        bar = store.read(2);
        assertNull("bar should be null", bar);
    }

}
