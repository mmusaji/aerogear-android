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
package org.aerogear.android.impl.core;

import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.aerogear.android.ReadFilter;
import org.json.JSONException;
import org.json.JSONObject;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RobolectricTestRunner.class)
public class ReadFilterTest {
    
    @Test
    public void testFilterQueryBuilder() throws JSONException {
        ReadFilter filter = new ReadFilter();
        filter.setLimit(1);
        assertEquals("limit=1", filter.getQuery());
        
        filter.setOffset(2);
        assertEquals("limit=1&offset=2", filter.getQuery());
        
        filter.setPerPage(4);
        assertEquals("limit=1&offset=2&per_page=4", filter.getQuery());
        
        filter.setWhere(new JSONObject("{\"model\":\"BMW\"}"));
        assertEquals("limit=1&offset=2&per_page=4&where=%7B%22model%22%3A%22BMW%22%7D", filter.getQuery());
        
    }
    
}
