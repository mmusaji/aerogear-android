/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aerogear.android.util;

import java.lang.reflect.Field;

/**
 *
 * @author summers
 */
public class TestUtil {
    public static void setPrivateField(Object target, String fieldName, Object value) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field field =  target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
