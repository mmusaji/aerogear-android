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

import android.content.Context;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import java.util.concurrent.CountDownLatch;
import org.aerogear.android.Callback;
import org.aerogear.android.impl.helper.Data;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RobolectricTestRunner.class)
public class SqlStoreTest {
    private Context context;
    private Data data;
    private SQLStore<Data> store;

    @Before
    public void setUp() {
        this.context = Robolectric.application.getApplicationContext();
        this.store = new SQLStore<Data>(Data.class, context);
        this.data = new Data("name", "description");
        this.data.setId(1);
    }

    @Test
    public void testSave() throws InterruptedException {
        saveData();
        Data readData = store.read(data.getId());
        Assert.assertEquals(data, readData);
    }

    @Test
    public void testReset() throws InterruptedException {
        saveData();
        store.reset();
        Data readData = store.read(data.getId());
        Assert.assertNull(readData);
    }

    private void saveData() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        store.open(new Callback() {

            @Override
            public void onSuccess(Object data) {
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                latch.countDown();
                throw new RuntimeException(e);
            }
        });
        latch.await();
        store.save(data);
    }

}
