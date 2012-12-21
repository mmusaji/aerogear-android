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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import org.aerogear.android.ReadFilter;
import org.aerogear.android.datamanager.Store;
import org.aerogear.android.datamanager.StoreType;

public class SQLStore<T> extends SQLiteOpenHelper implements Store<T> {

    private final Class<T> klass;

    private final static String CREATE_PARENT_TABLE = "create table %s "
            + " ( _ID integer primary key autoincrement)";

    private final static String CREATE_PROPERTIES_TABLE = "create table %s_property "
            + " ( _ID integer primary key autoincrement,"
            + "  PARENT_ID integer,"
            + "  PROPERTY_NAME text not null,"
            + "  PROPERTY_VALUE text )";

    private final static String CREATE_PROPERTIES_INDEXES = "create index %s_property_name_index "
            + " ON %s_property (PROPERTY_NAME);"
            + "create index %s_property_parent_index "
            + " ON %s_property (PARENT_ID);";

    public SQLStore(Class<T> klass, Context context) {
        super(context, klass.getSimpleName(), null, 1);
        this.klass = klass;
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public T read(Serializable id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<T> readWithFilter(ReadFilter filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void save(T item) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void remove(Serializable id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
