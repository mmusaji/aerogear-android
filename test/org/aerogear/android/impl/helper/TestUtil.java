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
package org.aerogear.android.impl.helper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.aerogear.android.impl.reflection.FieldNotFoundException;

public class TestUtil {

    public static void setPrivateField(Object target, String fieldName,
            Object value) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Class type = target.getClass();
        List<Field> fields = getAllFields(new ArrayList<Field>(), target.getClass());

        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                field.set(target, value);
                return;
            }
        }

        throw new FieldNotFoundException(target.getClass(), fieldName);

    }

    public static Object getPrivateField(Object target, String fieldName)
            throws NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    public static <T> T getPrivateField(Object target, String fieldName,
            Class<T> type) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        for (Field field : type.getDeclaredFields()) {
            fields.add(field);
        }

        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }
}
