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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.aerogear.android.Callback;
import org.aerogear.android.ReadFilter;
import org.aerogear.android.datamanager.IdGenerator;
import org.aerogear.android.datamanager.Store;
import org.aerogear.android.datamanager.StoreType;
import org.aerogear.android.impl.reflection.Property;
import org.aerogear.android.impl.reflection.Scan;
import org.json.JSONObject;

public class SQLStore<T> extends SQLiteOpenHelper implements Store<T> {

    private static final String TAG = SQLStore.class.getSimpleName();

    private final Class<T> klass;
    private final String className;

    private final static String CREATE_PROPERTIES_TABLE = "create table if not exists %s_property "
            + " ( _ID integer primary key autoincrement,"
            + "  PARENT_ID integer,"
            + "  PROPERTY_NAME text not null,"
            + "  PROPERTY_VALUE text )";

    private final static String CREATE_PROPERTIES_INDEXES = "create index  if not exists %s_property_name_index "
            + " ON %s_property (PROPERTY_NAME);"
            + "create index  if not exists %s_property_parent_index "
            + " ON %s_property (PARENT_ID);";
    private SQLiteDatabase database;
    private final Gson gson;
    private final IdGenerator generator = new DefaultIdGenerator();

    public SQLStore(Class<T> klass, Context context) {
        super(context, klass.getSimpleName(), null, 1);
        this.klass = klass;
        this.className = klass.getSimpleName();
        this.gson = new Gson();
    }

    public SQLStore(Class<T> klass, Context context, GsonBuilder builder) {
        super(context, klass.getSimpleName(), null, 1);
        this.klass = klass;
        this.className = klass.getSimpleName();
        this.gson = builder.create();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public StoreType getType() {
        return StoreTypes.SQL;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Collection<T> readAll() {
        String sql = String.format("Select PROPERTY_NAME, PROPERTY_VALUE,PARENT_ID from %s_property", className);
        Cursor cursor = database.rawQuery(sql, new String[0]);
        HashMap<Integer, JsonObject> objects = new HashMap<Integer, JsonObject>(cursor.getCount());
        try {
            while (cursor.moveToNext()) {
                Integer id = cursor.getInt(2);
                JsonObject object = objects.get(id);
                if (object == null) {
                    objects.put(id, object);
                }
                add(object, cursor.getString(0), cursor.getString(1));
            }
        } finally {
            cursor.close();
        }
        ArrayList<T> data = new ArrayList<T>(cursor.getCount());
        for (JsonObject object : objects.values()) {
            data.add(gson.fromJson(object, klass));
        }

        return data;

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public T read(Serializable id) {
        String sql = String.format("Select PROPERTY_NAME, PROPERTY_VALUE from %s_property where PARENT_ID = ?", className);
        String[] bindArgs = new String[1];
        bindArgs[0] = id.toString();
        JsonObject result = new JsonObject();
        Cursor cursor = database.rawQuery(sql, bindArgs);

        if (cursor.getCount() == 0) {
            return null;
        }

        try {
            while (cursor.moveToNext()) {
                add(result, cursor.getString(0), cursor.getString(1));
            }
        } finally {
            cursor.close();
        }

        return gson.fromJson(result, klass);

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<T> readWithFilter(ReadFilter filter) {
        JSONObject where = filter.getWhere();
        List<Pair<String, String>> keyValues = new ArrayList<Pair<String, String>>();
        buildKeyValuePairs(where, keyValues);
        scanForNestedObjectsInWhereClause(where);
        List<T> results = new ArrayList<T>(data.values());

        filterData(results, where);
        results = pageData(results, filter.getLimit(), filter.getOffset(), filter.getPerPage());
        return results;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void save(T item) {
        String recordIdFieldName = Scan.recordIdFieldNameIn(item.getClass());
        Property property = new Property(item.getClass(), recordIdFieldName);
        Serializable idValue = (Serializable) property.getValue(item);

        if (idValue == null) {
            idValue = generator.generate();
            property.setValue(item, idValue);
        }

        JsonObject serialized = (JsonObject) gson.toJsonTree(item, klass);
        saveElement(serialized, "", idValue);
    }

    private void saveElement(JsonObject serialized, String path, Serializable id) {
        String sql = String.format("insert into %s_property (PROPERTY_NAME, PROPERTY_VALUE, PARENT_ID) values (?,?,?)", className);
        Set<Entry<String, JsonElement>> members = serialized.entrySet();
        for (Entry<String, JsonElement> member : members) {
            JsonElement value = member.getValue();
            String propertyName = member.getKey();
            if (value.isJsonObject()) {
                saveElement((JsonObject) value, path + "." + propertyName, id);
            } else {
                database.execSQL(sql, new Object[] { propertyName, value, id });
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void reset() {
        String sql = String.format("Delete from %s_property", className);
        database.execSQL(sql);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void remove(Serializable id) {
        String sql = String.format("Delete from %s_property where PARENT_ID = ?", className);
        Object[] bindArgs = new Object[1];
        bindArgs[0] = id;
        database.execSQL(sql, bindArgs);

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(String.format(CREATE_PROPERTIES_TABLE, className));
        db.execSQL(String.format(CREATE_PROPERTIES_INDEXES, className, className, className, className));
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void open(final Callback onReady) {
        new AsyncTask<Void, Void, Void>() {
            private Exception exception;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    SQLStore.this.database = getWritableDatabase();
                    onReady.onSuccess(this);
                } catch (Exception e) {
                    this.exception = e;
                    Log.e(TAG, "There was an error loading the database", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (exception != null) {
                    onReady.onFailure(exception);
                } else {
                    onReady.onSuccess(SQLStore.this);
                }
            }

        }.execute((Void) null);
    }

    @Override
    public void close() {
        this.database.close();
    }

    private void add(JsonObject result, String propertyName, String propertyValue) {
        if (!propertyName.contains(".")) {
            result.addProperty(propertyName, propertyValue);
        } else {
            String[] names = propertyName.split("\\.", 2);
            JsonObject subObject = (JsonObject) result.get(names[0]);
            if (subObject == null) {
                subObject = new JsonObject();
                result.add(names[0], subObject);
            }
            add(subObject, names[1], propertyValue);

        }
    }

    private void buildKeyValuePairs(JSONObject where, List<Pair<String, String>> keyValues) {
        Iterator keys = where.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();

        }
    }
}
