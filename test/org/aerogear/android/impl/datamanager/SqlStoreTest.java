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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.aerogear.android.Callback;
import org.aerogear.android.ReadFilter;
import org.aerogear.android.RecordId;
import org.aerogear.android.impl.helper.Data;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RobolectricTestRunner.class)
public class SqlStoreTest {

    private Context context;
    private Data data;
    private SQLStore<Data> store;
    private SQLStore<TrivialNestedClass> nestedStore;

    @Before
    public void setUp() {
        this.context = Robolectric.application.getApplicationContext();
        this.store = new SQLStore<Data>(Data.class, context);
        this.nestedStore = new SQLStore<TrivialNestedClass>(TrivialNestedClass.class, context);
        this.data = new Data("name", "description");
        this.data.setId(10);
    }

    @Test
    public void testSave() throws InterruptedException {
        saveData(1, "name", "description");
        Data readData = store.read(data.getId());
        Assert.assertEquals(data, readData);
    }

    @Test
    public void testReset() throws InterruptedException {
        saveData(1, "name", "description");
        store.reset();
        Data readData = store.read(data.getId());
        Assert.assertNull(readData);
    }

    @Test
    public void testFilter() throws InterruptedException, JSONException {
        ReadFilter filter;
        JSONObject where;
        List<Data> result;

        saveData(1, "name", "description");
        saveData(2, "name", "description");
        saveData(3, "name2", "description");
        saveData(4, "name2", "description");
        saveData(5, "name", "description2");
        saveData(6, "name2", "description2");

        filter = new ReadFilter();
        where = new JSONObject();
        where.put("name", "name2");
        filter.setWhere(where);
        result = store.readWithFilter(filter);
        Assert.assertEquals(3, result.size());

        filter = new ReadFilter();
        where = new JSONObject();
        where.put("name", "name2");
        where.put("description", "description");
        filter.setWhere(where);
        result = store.readWithFilter(filter);
        Assert.assertEquals(2, result.size());

    }

    @Test
    public void testNestedSaveAndFilter() throws InterruptedException, JSONException {
        ReadFilter filter;
        JSONObject where;
        List<TrivialNestedClass> result;

        TrivialNestedClass newNested = new TrivialNestedClass();
        newNested.setId(1);
        newNested.setText("nestedText");
        newNested.setData(data);
        open(nestedStore);
        nestedStore.save(newNested);

        filter = new ReadFilter();
        where = new JSONObject();
        where.put("text", "nestedText");
        where.put("data.id", "10");
        filter.setWhere(where);
        result = nestedStore.readWithFilter(filter);
        Assert.assertEquals(1, result.size());
        TrivialNestedClass nestedResult = result.get(0);
        Assert.assertEquals("name", nestedResult.data.getName());

    }

    private void saveData(Integer id, String name, String desc) throws InterruptedException {
        open(store);
        store.save(new Data(id, name, desc));
    }

    private void open(SQLStore<?> store) throws InterruptedException {
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
    }

    public static final class TrivialNestedClass {

        @RecordId
        private Integer id;
        private String text;
        private Data data;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }
    }
}
