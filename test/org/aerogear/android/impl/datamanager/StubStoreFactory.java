package org.aerogear.android.impl.datamanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.aerogear.android.ReadFilter;
import org.aerogear.android.datamanager.IdGenerator;
import org.aerogear.android.datamanager.Store;
import org.aerogear.android.datamanager.StoreFactory;
import org.aerogear.android.datamanager.StoreType;

public class StubStoreFactory implements StoreFactory {
    @Override
    public Store createStore(StoreType type, IdGenerator idGenerator) {
        return new Store() {
            @Override
            public StoreType getType() {
                return new StoreType() {
                    @Override
                    public String getName() {
                        return "Stub";
                    }
                };
            }

            @Override
            public Collection readAll() {
                return new ArrayList();
            }

            @Override
            public Object read(Serializable id) {
                return new Object();
            }

            @Override
            public void save(Object item) {
            }

            @Override
            public void reset() {
            }

            @Override
            public void remove(Serializable id) {
            }

            @Override
            public List readWithFilter(ReadFilter filter) {
                return new ArrayList();
            }
        };
    }
}
