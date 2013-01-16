/*
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
package org.jboss.aerogear.android.util;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A collection of convenience methods for working with JSON data.
 */
public final class JsonUtils {

    private static final String TAG = JsonUtils.class.getSimpleName();

    private JsonUtils() {
    }

    /**
     *
     * @param gson
     * @param klass
     * @param json
     * @return
     */
    public static <T> T jsonToObject(Gson gson, Class<T> klass, String json) {
        T resultObject = gson.fromJson(json, klass);
        return resultObject;
    }

    /**
     *
     * @param gson a configured GSON instance.
     * @param klass the klass of the JSON string
     * @param json a single Json Object or a collection of Json objects which
     * are of type klass
     * @return A List of objects that the json parameter represents. If the
     * parameter is only a single object, then it will be inserted into a List.
     */
    public static <T> List<T> jsonToObjectList(Gson gson, Class<T> klass, String json) {
        JsonParser parser = new JsonParser();
        JsonElement result = parser.parse(json);
        if (result.isJsonArray()) {
            T[] resultArray = gson.fromJson(json, asArrayClass(klass));
            return Arrays.asList(resultArray);
        } else {
            T resultObject = gson.fromJson(json, klass);
            return asList(resultObject);
        }
    }

    /**
     * This will return a class of the type T[] from a given class. When we read
     * from the AG pipe, Java needs a reference to a generic array type.
     *
     * @param klass
     * @return
     */
    public static <T> Class<T[]> asArrayClass(Class<T> klass) {
        return (Class<T[]>) ((T[]) Array.newInstance(klass, 1)).getClass();
    }

    /**
     * Wraps item in a list
     * 
     * @param item
     * @return 
     */
    private static <T> List<T> asList(T item) {
        List<T> resultList = new ArrayList<T>(1);
        resultList.add(item);
        return resultList;
    }

    /**
     * 
     * This method calls jsonObject.put and wraps the JSONException as a RuntimeException if it is thrown.
     * 
     * @param jsonObject
     * @param key
     * @param value 
     */
    public static void add(JSONObject jsonObject, String key, Object value) {
        try {
            jsonObject.put(key, value);
        } catch (JSONException ex) {
            Log.e(TAG, String.format("Error adding {%s: %s}", key, value.toString()), ex);
        }
    }

    /**
     * Convert an instance of org.json.JSONObject to com.google.gson.JsonObject
     * @param where
     * @return 
     */
    public static JsonObject JSONToGson(JSONObject jsonObject) {
        return (JsonObject) new JsonParser().parse(jsonObject.toString());
    }
}
