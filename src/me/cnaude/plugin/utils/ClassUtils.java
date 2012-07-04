/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.cnaude.plugin.utils;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
 
/**
 * A utility class for accessing private fields, and calling private methods
 * @author WinSock
 * @version 1.0
 */
public class ClassUtils {
    @SuppressWarnings("unchecked")
    public static <T> T getPrivateField(Object object, String field) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        Field objectField = clazz.getDeclaredField(field);
        objectField.setAccessible(true);
        return (T)objectField.get(object);
    }
    public static void setPrivateField(Object object, String field, Object value) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        Field objectField = clazz.getDeclaredField(field);
        objectField.setAccessible(true);
        objectField.set(object, value);
    }
    public static Object callPrivateMethod(Object object, String method, Object[] params) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class<?> clazz = object.getClass();
        Class<?>[] paramClasses = new Class<?>[params.length];
        for (int i = 0; i < params.length; i++) {
            paramClasses[i] = params.getClass();
        }
        Method m = clazz.getDeclaredMethod(method, paramClasses);
        m.setAccessible(true);
        return m.invoke(object, params);
    }
}
 
 
