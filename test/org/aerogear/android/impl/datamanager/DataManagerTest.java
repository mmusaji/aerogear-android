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

import java.net.MalformedURLException;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import org.aerogear.android.DataManager;
import org.aerogear.android.datamanager.Store;
import static org.aerogear.android.impl.datamanager.StoreTypes.MEMORY;
import org.junit.Before;
import org.junit.Test;

public class DataManagerTest {

    private DataManager dataManager;

    @Before
    public void setup() {
        dataManager = new DataManager();
    }

    @Test
    public void testRegisterPipeFactory() throws MalformedURLException {
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        DataManager dataManager = new DataManager(new StubStoreFactory());
        
        Store store = dataManager.store("stub store");

        assertNotNull("store could not be null", store);
        assertEquals("verifying the type", "Stub", store.getType().getName());
    }

    @Test
    public void testCreateStoreWithDefaulType() {
        Store store = dataManager.store("foo");

        assertNotNull("store could not be null", store);
        assertEquals("verifying the type", MEMORY, store.getType());
    }

    @Test
    public void testCreateStoreWithMemoryType() {
        Store store = dataManager.store("foo", MEMORY);

        assertNotNull("store could not be null", store);
        assertEquals("verifying the type", MEMORY, store.getType());
    }
    @Test
    public void testAddStoreWithDefaulType() {
        dataManager.store("foo");
        Store store = dataManager.get("foo");

        assertNotNull("store could not be null", store);
        assertEquals("verifying the type", MEMORY, store.getType());
    }

    @Test
    public void testAddStoreWithMemoryType() {
        dataManager.store("foo", MEMORY);
        Store store = dataManager.get("foo");

        assertNotNull("foo store could not be null", store);
        assertEquals("verifying the type", MEMORY, store.getType());
    }

    @Test 
    public void testAndAddAndRemoveStores() {
        dataManager.store("foo", MEMORY);
        dataManager.store("bar");

        Store fooStore = dataManager.get("foo");
        assertNotNull("foo store could not be null", fooStore);
        
        Store barStore = dataManager.get("bar");
        assertNotNull("bar store could not be null", barStore);

        fooStore = dataManager.remove("foo");
        assertNotNull("foo store could not be null", fooStore);

        fooStore = dataManager.get("foo");
        assertNull("foo store should be null", fooStore);
    }

}